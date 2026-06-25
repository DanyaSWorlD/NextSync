package com.next.sync.core.sync.tasks

import com.next.sync.core.sync.model.Progress
import com.next.sync.core.sync.model.SynchronizableFile
import com.owncloud.android.lib.common.OwnCloudClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class DeleteLocalTask(
    private val file: SynchronizableFile
) : SyncTaskBase() {
    override fun run(client: OwnCloudClient,  progress: (Progress) -> Unit) {
        val localFile = File(file.fullPath)
        if (localFile.exists()) {
            if (localFile.isDirectory) {
                localFile.deleteRecursively()
            } else {
                localFile.delete()
            }
        }
    }

    override fun run(client: OwnCloudClient): Flow<Progress> = flow {
        withContext(Dispatchers.IO) {
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
}