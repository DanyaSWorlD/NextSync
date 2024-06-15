package com.next.sync.core.sync.strategy

import com.next.sync.core.sync.model.SynchronizableFile
import com.next.sync.core.sync.tasks.ISyncTask
import com.next.sync.core.sync.tasks.UploadTask

class SimpleUploadStrategy : ISyncStrategy {
    override fun decide(localFile: SynchronizableFile?, remoteFile: SynchronizableFile?): ISyncTask? {
        if(localFile == null || localFile.isFolder) return null

        if(localFile != remoteFile)
            return UploadTask(localFile)

        return null
    }
}