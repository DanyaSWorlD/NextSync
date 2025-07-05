package com.next.sync.core.sync.tasks

import com.next.sync.core.sync.model.SynchronizableFile
import com.owncloud.android.lib.common.OwnCloudClient
import java.io.File

class FolderLocalTask(
    private val localFile: SynchronizableFile,
    private val delete: Boolean
) : SyncTaskBase() {
    override fun run(client: OwnCloudClient) {
        val file = File(localFile.fullPath)

        if (delete) {
            if (file.exists())
                file.delete()
        } else if (!file.exists())
            file.mkdir()
    }
}