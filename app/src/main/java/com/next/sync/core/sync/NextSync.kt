package com.next.sync.core.sync

import com.next.sync.core.sync.model.Progress
import com.next.sync.core.sync.strategy.ISyncStrategy
import com.owncloud.android.lib.common.OwnCloudClient
import kotlinx.coroutines.flow.Flow

class NextSync(
    private val client: OwnCloudClient
) {
    fun sync(
        localPath: String,
        remotePath: String,
        strategy: ISyncStrategy,
        callback: ((Flow<Progress?>) -> Unit)? = null
    ) {
        var worker = SyncWorker(localPath, remotePath, strategy, client)
        if (callback != null)
            worker = worker.subscribe(callback)
        worker.sync()
    }
}