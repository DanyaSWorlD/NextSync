package com.next.sync.core.sync.tasks

import com.next.sync.core.sync.model.Progress
import com.next.sync.core.sync.model.SynchronizableFile
import com.owncloud.android.lib.common.OwnCloudClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class FolderLocalTask(
    private val localFile: SynchronizableFile,
    private val delete: Boolean
) : SyncTaskBase() {
    override fun run(client: OwnCloudClient, progress: (Progress) -> Unit) {
        val file = File(localFile.fullPath)

        if (delete) {
            if (file.exists())
                file.delete()
        } else if (!file.exists())
            file.mkdir()
    }

    override fun run(client: OwnCloudClient): Flow<Progress> = flow {
        withContext(Dispatchers.IO) {
            val file = File(localFile.fullPath)
            if (delete) {
                if (file.exists())
                    file.delete()
            } else if (!file.exists())
                file.mkdir()
        }
    }
}