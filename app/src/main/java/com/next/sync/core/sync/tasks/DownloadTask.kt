package com.next.sync.core.sync.tasks

import android.os.SystemClock
import android.util.Log
import com.next.sync.core.sync.model.Progress
import com.next.sync.core.sync.model.SynchronizableFile
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener
import com.owncloud.android.lib.resources.files.DownloadFileRemoteOperation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File

class DownloadTask(
    private val file: SynchronizableFile,
    private val remotePath: String
) : SyncTaskBase(), ILongRunningSyncTask {
    private val _progress = MutableStateFlow<Progress?>(null)
    override val progressFlow: Flow<Progress?> = _progress

    override fun run(client: OwnCloudClient, progress: (Progress) -> Unit) {
        val localFile = File(file.fullPath)
        localFile.parentFile?.let { parent ->
            if (!parent.exists()) {
                parent.mkdirs()
            }
        }

        val operation = DownloadFileRemoteOperation(remotePath, localFile.absolutePath)
        operation.addDatatransferProgressListener { progressRate, totalTransferredSoFar, totalToTransfer, fileName ->
            progress(
                Progress(progressRate, totalTransferredSoFar, totalToTransfer, fileName)
            )
        }

        operation.execute(client)
    }

    override fun run(client: OwnCloudClient): Flow<Progress> = callbackFlow {
        val localFile = File(file.fullPath)
        localFile.parentFile?.let { parent ->
            if (!parent.exists()) {
                parent.mkdirs()
            }
        }

        val operation = DownloadFileRemoteOperation(remotePath, localFile.absolutePath)
        var lastEmitMs = 0L
        val listener = OnDatatransferProgressListener { progressRate, totalTransferredSoFar, totalToTransfer, fileName ->
            val now = SystemClock.elapsedRealtime()
            if (now - lastEmitMs < 200L) return@OnDatatransferProgressListener
            lastEmitMs = now
            trySend(
                Progress(progressRate, totalTransferredSoFar, totalToTransfer, fileName)
            ).isSuccess
        }

        operation.addDatatransferProgressListener(listener)

        withContext(Dispatchers.IO) {
            operation.execute(client)
        }

        close()
    }
}