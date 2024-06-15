package com.next.sync.core.sync

import com.next.sync.core.di.NextcloudClientHelper
import com.next.sync.core.sync.model.SynchronizableFile
import com.next.sync.core.sync.strategy.ISyncStrategy
import com.next.sync.core.sync.tasks.ISyncTask
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import java.io.File

class SyncWorker(
    val localPath: String,
    val remotePath: String,
    val strategy: ISyncStrategy,
    val nextcloudHelper: NextcloudClientHelper
) {
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

    private fun run(task: ISyncTask?) {
        if (task == null) return
        var current: ISyncTask = task
        while (true) {
            current.Run()
            if (!current.next)
                continue
            current = current.next()
        }
    }

    private fun getRemoteFiles(path: String): List<RemoteFile> {
        val client = nextcloudHelper.ownCloudClient
        val result =
            ReadFolderRemoteOperation(path).execute(client!!)
        val response = result.data as List<RemoteFile>
        return response.subList(1, response.size)
    }
}