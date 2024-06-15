package com.next.sync.core.sync

import com.next.sync.core.di.NextcloudClientHelper
import com.next.sync.core.sync.strategy.ISyncStrategy
import com.next.sync.core.sync.strategy.SimpleUploadStrategy

class NextSync {

    val simpleUpload: SimpleUploadStrategy by lazy { SimpleUploadStrategy() }

    fun sync(
        localPath: String,
        remotePath: String,
        strategy: ISyncStrategy,
        nextcloudHelper: NextcloudClientHelper
    ) {

    }
}