package com.next.sync.core.sync.tasks

import com.next.sync.core.sync.model.Progress
import com.next.sync.core.sync.model.SynchronizableFile
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class FolderRemoteTask(
    private val file: SynchronizableFile,
    private val remotePath: String,
    private val delete: Boolean
) : SyncTaskBase() {
    override fun run(client: OwnCloudClient, progress: (Progress) -> Unit) {
        if (delete)
            RemoveFileRemoteOperation(remotePath).execute(client)
        else
            UploadFileRemoteOperation(
                file.fullPath,
                remotePath,
                "DIR",
                file.edited / 1000
            )
    }

    override fun run(client: OwnCloudClient): Flow<Progress> = flow {
        withContext(Dispatchers.IO) {
            if (delete)
                RemoveFileRemoteOperation(remotePath).execute(client)
            else
                UploadFileRemoteOperation(
                    file.fullPath,
                    remotePath,
                    "DIR",
                    file.edited / 1000
                )
        }
    }
}