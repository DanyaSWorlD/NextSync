package com.next.sync.core.sync

import com.next.sync.core.sync.model.Progress
import com.next.sync.core.sync.model.SynchronizableFile
import com.next.sync.core.sync.strategy.ISyncStrategy
import com.next.sync.core.sync.tasks.ILongRunningSyncTask
import com.next.sync.core.sync.tasks.ISyncTask
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import kotlinx.coroutines.flow.Flow
import java.io.File

class SyncWorker(
    val localPath: String,
    val remotePath: String,
    val strategy: ISyncStrategy,
    val client: OwnCloudClient
) {
    var callback: ((Flow<Progress?>) -> Unit)? = null

    fun sync(relativePath: String = "") {
        val localFiles = (File(localPath).listFiles() ?: arrayOf())
            .map { SynchronizableFile.from(it, localPath) }
            .associateBy { it.relativePath }
        val remoteFiles = getRemoteFiles(remotePath)
            .map { SynchronizableFile.from(it, remotePath) }
            .associateBy { it.relativePath }

        val files = (localFiles.keys + remoteFiles.keys)
            .distinct()
            .associateBy({ it }) { Pair(localFiles[it], remoteFiles[it]) }

        for (file in files) {
            if (file.value.first?.isFolder == true || file.value.second?.isFolder == true)
                sync(file.value.first?.relativePath ?: file.value.second?.relativePath!!)

            run(strategy.decide(file.value.first, file.value.second))
        }
    }

    fun subscribe(callback: (Flow<Progress?>) -> Unit): SyncWorker {
        this.callback = callback
        return this
    }

    private fun run(task: ISyncTask?) {
        if (task == null) return
        var current: ISyncTask = task
        while (true) {
            notifyProgress(current)
            current.run(client)
            if (!current.hasNext)
                break
            current = current.next()
        }
    }

    private fun notifyProgress(task: ISyncTask) {
        if (task is ILongRunningSyncTask) {
            callback?.let { it(task.progressFlow) }
        }
    }

    private fun getRemoteFiles(path: String): List<RemoteFile> {
        val result =
            ReadFolderRemoteOperation(path).execute(client)
        val response = result.data as List<RemoteFile>
        return response.subList(1, response.size)
    }
}