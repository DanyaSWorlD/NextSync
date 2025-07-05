package com.next.sync.core.di

import com.next.sync.core.db.ObjectBox
import com.next.sync.core.db.data.FileStateEntity
import com.next.sync.core.db.data.FileStateEntity_
import com.next.sync.core.db.data.TaskEntity
import com.next.sync.core.model.FileStateItem
import com.next.sync.core.sync.NextSync
import com.next.sync.core.sync.strategy.SimpleUploadStrategy
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class SynchronizationModule @Inject constructor(
    private val nextcloudHelper: NextcloudClientHelper,
    private val dataBus: DataBus
) {
    private val nextSync: NextSync by lazy { NextSync(nextcloudHelper.ownCloudClient!!) }

    suspend fun sync() {
        val taskBox = ObjectBox.store.boxFor(TaskEntity::class)
        val task = taskBox.query { }.findFirst() ?: return

        runBlocking {
            launch {
                nextSync.sync(
                    task.localPath,
                    task.remotePath,
                    SimpleUploadStrategy(task.remotePath)
                ) {
                    launch(Dispatchers.IO) {
                        dataBus.emit(
                            DataBusKey.ProgressFlowReset,
                            it
                        )
                    }
                }
            }
        }

    }

    fun synchronizeTask() {
        val taskBox = ObjectBox.store.boxFor(TaskEntity::class)
        val stateBox = ObjectBox.store.boxFor(FileStateEntity::class)

        val task = taskBox.query { }.findFirst() ?: return
        val state = stateBox.query(FileStateEntity_.taskId.equal(task.id)).build().findFirst()

        val diff = getDiff(task, state)

        for (d in diff.diff) {

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

            // key exist, conflict occurred
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
        val client = nextcloudHelper.ownCloudClient
        val result =
            ReadFolderRemoteOperation(path).execute(client!!)
        val response = result.data as List<RemoteFile>
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