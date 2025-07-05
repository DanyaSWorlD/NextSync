package com.next.sync.core.sync.tasks

import com.next.sync.core.sync.model.Progress
import com.next.sync.core.sync.model.SynchronizableFile
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.resources.files.DownloadFileRemoteOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import java.io.File

class DownloadTask(
    private val file: SynchronizableFile,
    private val remotePath: String
) : SyncTaskBase(), ILongRunningSyncTask {
    private val _progress = MutableStateFlow<Progress?>(null)
    override val progressFlow: Flow<Progress?> = _progress

    override fun run(client: OwnCloudClient) {
        val localFile = File(file.fullPath)
        if (!localFile.parentFile?.exists()!!) {
            localFile.parentFile?.mkdirs()
        }

        val operation = DownloadFileRemoteOperation(remotePath, localFile.absolutePath)
        operation.addDatatransferProgressListener { progressRate, totalTransferredSoFar, totalToTransfer, fileName ->
            runBlocking {
                _progress.emit(
                    Progress(progressRate, totalTransferredSoFar, totalToTransfer, fileName)
                )
            }
        }
        
        operation.execute(client)
    }
}