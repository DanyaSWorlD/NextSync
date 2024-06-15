package com.next.sync.core.sync.tasks

import com.next.sync.core.sync.model.SynchronizableFile
import com.owncloud.android.lib.common.OwnCloudClient

class FolderRemoteTask(
    private val file: SynchronizableFile,
    private val delete: Boolean
) : SyncTaskBase() {
    override fun run(client: OwnCloudClient) {

    }
}