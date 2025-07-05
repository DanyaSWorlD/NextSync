package com.next.sync.core.sync.tasks

import com.next.sync.core.sync.model.SynchronizableFile
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation

class DeleteRemoteTask(
    private val file: SynchronizableFile
) : SyncTaskBase() {
    override fun run(client: OwnCloudClient) {
        RemoveFileRemoteOperation(file.relativePath).execute(client)
    }
}