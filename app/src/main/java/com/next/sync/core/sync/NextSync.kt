package com.next.sync.core.sync

import com.next.sync.core.sync.strategy.ISyncStrategy
import com.owncloud.android.lib.common.OwnCloudClient

class NextSync(
    private val client: OwnCloudClient
) {
    fun sync(
        localPath: String,
        remotePath: String,
        strategy: ISyncStrategy
    ) {
        SyncWorker(localPath, remotePath, strategy, client).sync()
    }
}