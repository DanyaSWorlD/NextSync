package com.next.sync.core.sync.strategy

import com.next.sync.core.sync.model.SynchronizableFile
import com.next.sync.core.sync.tasks.ISyncTask

interface ISyncStrategy {
    fun decide(localFile : SynchronizableFile?, remoteFile: SynchronizableFile?) : ISyncTask?
}