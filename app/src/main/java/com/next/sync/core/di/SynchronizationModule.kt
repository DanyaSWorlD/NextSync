package com.next.sync.core.di

import com.next.sync.core.db.ObjectBox
import com.next.sync.core.db.data.FileStateEntity
import com.next.sync.core.db.data.FileStateEntity_
import com.next.sync.core.db.data.TaskEntity
import com.next.sync.core.model.FileStateItem
import com.next.sync.core.sync.NextSync
import com.next.sync.core.sync.SyncProgressTracker
import com.next.sync.core.sync.model.SynchronizableFile
import com.next.sync.core.sync.strategy.SimpleUploadStrategy
import com.next.sync.core.sync.tasks.DeleteLocalTask
import com.next.sync.core.sync.tasks.DeleteRemoteTask
import com.next.sync.core.sync.tasks.DownloadTask
import com.next.sync.core.sync.tasks.UploadTask
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.query
import kotlinx.coroutines.yield
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class SynchronizationModule @Inject constructor(
    private val nextcloudHelper: NextcloudClientHelper,
    private val dataBus: DataBus,
    private val progressTracker: SyncProgressTracker,
) {
    private var nextSync: NextSync? = null

    suspend fun sync() {
        val c = nextcloudHelper.ownCloudClient
        if (c == null) {
            nextcloudHelper.loadService()
            if (nextcloudHelper.ownCloudClient == null) return
        }
        val client = nextcloudHelper.ownCloudClient ?: return

        if (nextSync == null) {
            nextSync = NextSync(client)
        }
        val ns = nextSync!!

        val taskBox = ObjectBox.store.boxFor(TaskEntity::class)
        val tasks = taskBox.all
        if (tasks.isEmpty()) return

        data class ScanResult(
            val localFiles: Map<String, SynchronizableFile>,
            val remoteFiles: Map<String, SynchronizableFile>,
            val strategy: SimpleUploadStrategy
        )

        var totalFiles = 0
        var totalBytes = 0L
        val scans = mutableListOf<ScanResult>()

        for (task in tasks) {
            val localFiles = ns.getLocalFiles("", task.localPath)
            val remoteFiles = ns.getRemoteFiles("", task.remotePath)
            val strategy = SimpleUploadStrategy(task.remotePath)
            val allPaths = (localFiles.keys + remoteFiles.keys).distinct()

            var taskHasWork = false
            for (path in allPaths) {
                val syncTask = strategy.decide(localFiles[path], remoteFiles[path])
                if (syncTask != null) {
                    totalFiles++
                    totalBytes += localFiles[path]?.size ?: remoteFiles[path]?.size ?: 0
                    taskHasWork = true
                }
            }

            if (taskHasWork) {
                scans.add(ScanResult(localFiles, remoteFiles, strategy))
            }
        }

        progressTracker.start(0, 0)

        if (totalFiles == 0) {
            progressTracker.finish()
            return
        }

        progressTracker.updateTotals(totalFiles, totalBytes)

        try {
            for (scan in scans) {
                val allPaths = (scan.localFiles.keys + scan.remoteFiles.keys).distinct()
                for (path in allPaths) {
                    yield()
                    val syncTask = scan.strategy.decide(scan.localFiles[path], scan.remoteFiles[path])
                    if (syncTask != null) {
                        ns.executeTask(syncTask) { progress ->
                            progressTracker.onFileProgress(
                                progress.rate, progress.done, progress.total, progress.fileName
                            )

                            if (progress.done >= progress.total) {
                                progressTracker.onFileComplete(progress.fileName, progress.total)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SynchronizationModule", "Sync failed: ${e.message}")
            progressTracker.onError(e.message ?: "Unknown error")
        }

        progressTracker.finish()
    }

    fun synchronizeTask() {
        val taskBox = ObjectBox.store.boxFor(TaskEntity::class)
        val stateBox = ObjectBox.store.boxFor(FileStateEntity::class)

        val task = taskBox.all.firstOrNull() ?: return
        val state = stateBox.query(FileStateEntity_.taskId.equal(task.id)).build().findFirst()

        val diff = getDiff(task, state)

        val client = nextcloudHelper.ownCloudClient ?: return

        for ((_, syncDiff) in diff.diff) {
            executeSyncDiff(syncDiff, task, client)
        }

        for (conflict in diff.conflicts) {
            executeSyncDiff(conflict.first, task, client)
        }
    }

    private fun executeSyncDiff(syncDiff: SyncDiff, task: TaskEntity, client: OwnCloudClient) {
        val file = syncDiff.file
        val synchronizableFile = SynchronizableFile(
            name = getName(file.relativePath),
            relativePath = file.relativePath,
            fullPath = task.localPath + file.relativePath,
            size = file.fileSize,
            edited = file.lastEdited,
            isFolder = file.isFolder
        )
        val remoteFilePath = task.remotePath + file.relativePath

        when (syncDiff.option) {
            SyncOption.Upload -> {
                UploadTask(synchronizableFile, remoteFilePath).run(client)
            }
            SyncOption.Download -> {
                DownloadTask(synchronizableFile, remoteFilePath).run(client)
            }
            SyncOption.DeleteLocal -> {
                DeleteLocalTask(synchronizableFile).run(client)
            }
            SyncOption.DeleteRemote -> {
                DeleteRemoteTask(synchronizableFile).run(client)
            }
            SyncOption.Ignore -> { }
        }
    }

    fun getDiff(task: TaskEntity, state: FileStateEntity?): DiffWithConflicts {
        var fileStateItem: FileStateItem? = null
        if (state != null)
            fileStateItem = Json.decodeFromString<FileStateItem>(state.folderContent)

        val remoteDiff = getRemoteDiffRecursive(task.remotePath, fileStateItem, task.remotePath)
        val localDiff = getLocalDiffRecursive(File(task.localPath), fileStateItem, task.localPath)

        val conflicts: MutableList<Pair<SyncDiff, SyncDiff>> = mutableListOf()
        val completeDiff: MutableMap<String, SyncDiff> =
            localDiff.associate {
                it.file.relativePath to SyncDiff(
                    it.file, when (it.option) {
                        StateOption.Add -> SyncOption.Upload
                        StateOption.Update -> SyncOption.Upload
                        StateOption.Remove -> SyncOption.DeleteLocal
                    }
                )
            }.toMutableMap()

        for (remote in remoteDiff) {
            val key = remote.file.relativePath
            if (!completeDiff.containsKey(key)) {
                completeDiff[key] = SyncDiff(remote.file, SyncOption.Download)
                continue
            }

            val existing = completeDiff[key]
            if (existing!!.option == SyncOption.DeleteLocal && remote.option == StateOption.Remove)
                continue

            if (existing.file.fileSize == remote.file.fileSize
                && existing.file.lastEdited == remote.file.lastEdited
            ) {
                completeDiff[key] = existing.copy(option = SyncOption.Ignore)
                continue
            }

            completeDiff.remove(key)
            conflicts.add(
                Pair(
                    existing, SyncDiff(
                        remote.file, option = when (remote.option) {
                            StateOption.Add -> SyncOption.Upload
                            StateOption.Update -> SyncOption.Upload
                            StateOption.Remove -> SyncOption.DeleteRemote
                        }
                    )
                )
            )
        }

        return DiffWithConflicts(completeDiff, conflicts)
    }

    private fun getRemoteDiffRecursive(
        path: String?,
        state: FileStateItem?,
        taskRootPath: String
    ): MutableList<StateDiff> {
        val diff: MutableList<StateDiff> = mutableListOf()

        val stateFiles =
            state?.child?.associateBy { getName(it.relativePath) }?.toMutableMap() ?: mutableMapOf()
        val remoteFiles = getRemoteFiles(path!!)

        for (remoteFile in remoteFiles) {
            val name = getName(remoteFile.remotePath!!)

            if (remoteFile.mimeType == "DIR") {
                var child: FileStateItem? = null
                if (stateFiles.containsKey(name))
                    child = stateFiles[name]
                else {
                    diff.add(
                        StateDiff(
                            remoteToStateFile(remoteFile, taskRootPath, true),
                            StateOption.Add
                        )
                    )
                    stateFiles.remove(name)
                }

                diff.addAll(getRemoteDiffRecursive(remoteFile.remotePath, child, taskRootPath))
            }

            if (stateFiles.containsKey(name)) {
                val stateFile = stateFiles[name]

                if (remoteFile.size == stateFile?.fileSize)
                    continue

                diff.add(StateDiff(remoteToStateFile(remoteFile, taskRootPath), StateOption.Update))
                stateFiles.remove(name)
            } else {
                diff.add(StateDiff(remoteToStateFile(remoteFile, taskRootPath), StateOption.Add))
                stateFiles.remove(name)
            }
        }

        if (stateFiles.any())
            diff.addAll(stateFiles.map { StateDiff(it.value, StateOption.Remove) })

        return diff
    }

    private fun getLocalDiffRecursive(
        file: File?,
        state: FileStateItem?,
        taskRootPath: String
    ): MutableList<StateDiff> {
        val diff: MutableList<StateDiff> = mutableListOf()

        val stateFiles =
            state?.child?.associateBy { getName(it.relativePath) }?.toMutableMap() ?: mutableMapOf()
        val localFiles = file?.listFiles() ?: arrayOf()

        for (localFile in localFiles) {
            val name = getName(localFile.path)

            if (localFile.isDirectory) {
                var child: FileStateItem? = null
                if (stateFiles.containsKey(name))
                    child = stateFiles[name]
                else {
                    diff.add(StateDiff(fileToStateFile(localFile, taskRootPath), StateOption.Add))
                    stateFiles.remove(name)
                }

                diff.addAll(getLocalDiffRecursive(localFile, child, taskRootPath))
            }

            if (stateFiles.containsKey(name)) {
                val stateFile = stateFiles[name]

                if (localFile.length() == stateFile?.fileSize)
                    continue

                diff.add(StateDiff(fileToStateFile(localFile, taskRootPath), StateOption.Update))
                stateFiles.remove(name)
            } else {
                diff.add(StateDiff(fileToStateFile(localFile, taskRootPath), StateOption.Add))
                stateFiles.remove(name)
            }
        }

        if (stateFiles.any())
            diff.addAll(stateFiles.map { StateDiff(it.value, StateOption.Remove) })

        return diff
    }

    private fun getRemoteFiles(path: String): List<RemoteFile> {
        val client = nextcloudHelper.ownCloudClient ?: return emptyList()
        val result = ReadFolderRemoteOperation(path).execute(client)
        if (!result.isSuccess) return emptyList()
        @Suppress("UNCHECKED_CAST")
        val response = result.data as? List<RemoteFile> ?: return emptyList()
        return response.subList(1, response.size)
    }

    private fun getName(file: String): String {
        return file.split("/").last { x -> x.isNotEmpty() }
    }

    private fun fileToStateFile(file: File, taskRootPath: String): FileStateItem {
        val newStateItem = FileStateItem(relativePath(taskRootPath, file.path))

        newStateItem.fileSize = file.length()
        newStateItem.lastEdited = file.lastModified()
        newStateItem.isFolder = file.isDirectory

        return newStateItem
    }

    private fun remoteToStateFile(
        remote: RemoteFile,
        taskRootPath: String,
        isFolder: Boolean = false
    ): FileStateItem {
        val newStateItem = FileStateItem(relativePath(taskRootPath, remote.remotePath!!))

        newStateItem.tag = remote.etag
        newStateItem.fileSize = remote.size
        newStateItem.lastEdited = remote.modifiedTimestamp
        newStateItem.isFolder = isFolder

        return newStateItem
    }

    private fun relativePath(basePath: String, path: String): String {
        return path.removePrefix(basePath)
    }

    public data class DiffWithConflicts(
        val diff: MutableMap<String, SyncDiff>,
        val conflicts: MutableList<Pair<SyncDiff, SyncDiff>>
    )

    public data class StateDiff(
        val file: FileStateItem,
        val option: StateOption
    )

    public data class SyncDiff(
        val file: FileStateItem,
        val option: SyncOption
    )

    public enum class SyncOption {
        Upload,
        Download,
        Ignore,
        DeleteLocal,
        DeleteRemote
    }

    public enum class StateOption {
        Add,
        Remove,
        Update
    }
}
