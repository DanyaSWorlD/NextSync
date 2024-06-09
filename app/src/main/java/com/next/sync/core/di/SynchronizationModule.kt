package com.next.sync.core.di

import com.next.sync.core.db.ObjectBox
import com.next.sync.core.db.data.FileStateEntity
import com.next.sync.core.db.data.FileStateEntity_
import com.next.sync.core.db.data.TaskEntity
import com.next.sync.core.model.FileStateItem
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.query
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class SynchronizationModule @Inject constructor(
    private var nextcloudHelper: NextcloudClientHelper
) {
    fun synchronizeTask() {
        val taskBox = ObjectBox.store.boxFor(TaskEntity::class)
        val stateBox = ObjectBox.store.boxFor(FileStateEntity::class)

        val task = taskBox.query { }.findFirst() ?: return
        var state = stateBox.query(FileStateEntity_.taskId.equal(task.id)).build().findFirst()

        var fileStateItem: FileStateItem? = null
        if (state != null)
            fileStateItem = Json.decodeFromString<FileStateItem>(state.folderContent)

        val remoteDiff = getRemoteDiffRecursive(task.remotePath, fileStateItem)
        val localDiff = getLocalDiffRecursive(File(task.localPath), fileStateItem)


    }

    private fun getRemoteDiffRecursive(
        path: String?,
        state: FileStateItem?
    ): MutableList<FileDiff> {
        val diff: MutableList<FileDiff> = mutableListOf()

        val stateFiles = state!!.child.associateBy { getName(it.relativePath) }.toMutableMap()
        val remoteFiles = getRemoteFiles(path!!)

        for (remoteFile in remoteFiles) {
            val name = getName(remoteFile.remotePath!!)

            if (remoteFile.mimeType == "DIR") {
                var child: FileStateItem? = null
                if (stateFiles.containsKey(name))
                    child = stateFiles[name]
                else {
                    diff.add(FileDiff(remoteToStateFile(remoteFile), SyncOption.Add))
                    stateFiles.remove(name)
                }

                diff.addAll(getRemoteDiffRecursive(remoteFile.remotePath, child))
            }

            if (stateFiles.containsKey(name)) {
                val stateFile = stateFiles[name]

                if (remoteFile.size == stateFile?.fileSize)
                    continue

                diff.add(FileDiff(remoteToStateFile(remoteFile), SyncOption.Update))
                stateFiles.remove(name)
            } else {
                diff.add(FileDiff(remoteToStateFile(remoteFile), SyncOption.Add))
                stateFiles.remove(name)
            }
        }

        if (stateFiles.any())
            diff.addAll(stateFiles.map { FileDiff(it.value, SyncOption.Remove) })

        return diff
    }

    private fun getLocalDiffRecursive(
        file: File?,
        state: FileStateItem?
    ): MutableList<FileDiff> {
        val diff: MutableList<FileDiff> = mutableListOf()

        val stateFiles = state!!.child.associateBy { getName(it.relativePath) }.toMutableMap()
        val localFiles = file?.listFiles() ?: arrayOf()

        for (localFile in localFiles) {
            val name = getName(localFile.path)

            if (localFile.isDirectory) {
                var child: FileStateItem? = null
                if (stateFiles.containsKey(name))
                    child = stateFiles[name]
                else {
                    diff.add(FileDiff(fileToStateFile(localFile), SyncOption.Add))
                    stateFiles.remove(name)
                }

                diff.addAll(getLocalDiffRecursive(localFile, child))
            }

            if (stateFiles.containsKey(name)) {
                val stateFile = stateFiles[name]

                if (localFile.length() == stateFile?.fileSize)
                    continue

                diff.add(FileDiff(fileToStateFile(localFile), SyncOption.Update))
                stateFiles.remove(name)
            } else {
                diff.add(FileDiff(fileToStateFile(localFile), SyncOption.Add))
                stateFiles.remove(name)
            }
        }

        if (stateFiles.any())
            diff.addAll(stateFiles.map { FileDiff(it.value, SyncOption.Remove) })

        return diff
    }

    private fun getRemoteFiles(path: String): List<RemoteFile> {
        val client = nextcloudHelper.ownCloudClient
        val result =
            ReadFolderRemoteOperation(path).execute(client!!)
        return result.data as List<RemoteFile>
    }

    private fun getName(file: String): String {
        return file.split("/").last { x -> x.isNotEmpty() }
    }

    private fun fileToStateFile(file: File): FileStateItem {
        val newStateItem = FileStateItem(file.path)

        newStateItem.fileSize = file.length()
        newStateItem.lastEdited = file.lastModified()
        newStateItem.isFolder = file.isDirectory

        return newStateItem
    }

    private fun remoteToStateFile(remote: RemoteFile, isFolder: Boolean = false): FileStateItem {
        val newStateItem = FileStateItem(remote.remotePath!!)
        newStateItem.tag = remote.etag
        newStateItem.fileSize = remote.size
        newStateItem.lastEdited = remote.modifiedTimestamp
        newStateItem.isFolder = isFolder

        return newStateItem
    }

    public data class FileDiff(
        val file: FileStateItem,
        val option: SyncOption
    )

    public enum class SyncOption {
        Add,
        Remove,
        Update
    }
}