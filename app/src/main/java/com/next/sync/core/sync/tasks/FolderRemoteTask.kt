package com.next.sync.core.sync.tasks

import com.next.sync.core.sync.model.SynchronizableFile
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation

class FolderRemoteTask(
    private val file: SynchronizableFile,
    private val remotePath: String,
    private val delete: Boolean
) : SyncTaskBase() {
    override fun run(client: OwnCloudClient) {
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