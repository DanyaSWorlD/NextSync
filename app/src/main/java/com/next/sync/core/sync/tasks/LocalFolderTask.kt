package com.next.sync.core.sync.tasks

import com.next.sync.core.sync.model.SynchronizableFile
import java.io.File

class FolderLocalTask(
    private val localFile: SynchronizableFile,
    private val delete: Boolean
) : SyncTaskBase() {
    override fun Run() {
        val file = File(localFile.fullPath)

        if (delete) {
            if (file.exists())
                file.delete()
        } else if (!file.exists())
            file.mkdir()
    }
}