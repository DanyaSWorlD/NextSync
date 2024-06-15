package com.next.sync.core.sync.tasks

import android.webkit.MimeTypeMap
import com.next.sync.core.sync.model.SynchronizableFile
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation


class UploadTask(
    private val localFile: SynchronizableFile, private val remotePath: String
) : SyncTaskBase() {
    override fun run(client: OwnCloudClient) {
        val upload = UploadFileRemoteOperation(
            localFile.fullPath,
            remotePath,
            getMimeType(localFile.relativePath),
            localFile.edited / 1000
        )
        upload.execute(client)
    }

    private fun getMimeType(path: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(path)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }
}