package com.next.sync.core.sync.tasks

import com.owncloud.android.lib.common.OwnCloudClient

interface ISyncTask {
    val hasNext: Boolean get

    fun next(): ISyncTask

    fun run(client: OwnCloudClient)
}