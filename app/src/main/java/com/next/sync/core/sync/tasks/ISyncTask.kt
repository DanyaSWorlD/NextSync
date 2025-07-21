package com.next.sync.core.sync.tasks

import com.next.sync.core.sync.model.Progress
import com.owncloud.android.lib.common.OwnCloudClient

interface ISyncTask {
    val hasNext: Boolean get

    fun next(): ISyncTask

    fun run(client: OwnCloudClient, progress: (Progress) -> Unit = {})
}