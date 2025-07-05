package com.next.sync.core.sync.tasks

import com.next.sync.core.sync.model.SynchronizableFile
import com.owncloud.android.lib.common.OwnCloudClient
import java.io.File

class DeleteLocalTask(
    private val file: SynchronizableFile
) : SyncTaskBase() {
    override fun run(client: OwnCloudClient) {
        val localFile = File(file.fullPath)
        if (localFile.exists()) {
            if (localFile.isDirectory) {
                localFile.deleteRecursively()
            } else {
                localFile.delete()
            }
        }
    }
}