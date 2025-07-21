package com.next.sync.core.sync.tasks

import com.next.sync.core.sync.model.Progress
import com.next.sync.core.sync.model.SynchronizableFile
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation
import kotlinx.coroutines.flow.Flow

class DeleteRemoteTask(
    private val file: SynchronizableFile
) : SyncTaskBase() {
    override fun run(client: OwnCloudClient, progress: (Progress) -> Unit) {
        RemoveFileRemoteOperation(file.relativePath).execute(client)
    }

    override fun run(client: OwnCloudClient): Flow<Progress> {
        TODO("Not yet implemented")
    }
}