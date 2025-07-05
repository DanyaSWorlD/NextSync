package com.next.sync.core.sync.strategy

import com.next.sync.core.sync.model.SynchronizableFile
import com.next.sync.core.sync.tasks.ISyncTask
import com.next.sync.core.model.SyncFlowDirection

interface ISyncStrategy {
    val syncDirection: SyncFlowDirection
    val deleteRemote: Boolean
    val deleteLocal: Boolean
    val conflictResolution: ConflictResolutionStrategy
    
    fun decide(localFile: SynchronizableFile?, remoteFile: SynchronizableFile?): ISyncTask?
    
    enum class ConflictResolutionStrategy {
        LOCAL_WINS,
        REMOTE_WINS,
        NEWER_WINS,
        ASK_USER,
        KEEP_BOTH
    }
}