package com.next.sync.core.sync.strategy

import com.next.sync.core.model.SyncFlowDirection
import com.next.sync.core.sync.model.SynchronizableFile
import com.next.sync.core.sync.tasks.FolderRemoteTask
import com.next.sync.core.sync.tasks.ISyncTask
import com.next.sync.core.sync.tasks.UploadTask

class SimpleUploadStrategy(remoteBasePath: String) : ISyncStrategy {
    private val normalizedBase: String by lazy { if (remoteBasePath.endsWith("/")) remoteBasePath else "$remoteBasePath/" }
    override val syncDirection: SyncFlowDirection = SyncFlowDirection.ToCloud
    override val deleteRemote: Boolean = false
    override val deleteLocal: Boolean = false
    override val conflictResolution: ISyncStrategy.ConflictResolutionStrategy =
        ISyncStrategy.ConflictResolutionStrategy.LOCAL_WINS

    override fun decide(
        localFile: SynchronizableFile?, remoteFile: SynchronizableFile?
    ): ISyncTask? {
        if (localFile == null) return null

        if (localFile.isFolder) {
            if (remoteFile == null)
                return FolderRemoteTask(localFile, normalizedBase + localFile.relativePath, false)
            else
                return null
        }

        if (localFile != remoteFile) return UploadTask(
            localFile, normalizedBase + localFile.relativePath
        )

        return null
    }
}