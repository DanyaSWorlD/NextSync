package com.next.sync.core.sync.model

import com.owncloud.android.lib.resources.files.model.RemoteFile
import java.io.File

public data class SynchronizableFile(
    val name: String,
    val relativePath: String,
    val fullPath: String,
    val size: Long,
    val edited: Long,
    val isFolder: Boolean
) {
    companion object {
        fun from(file: RemoteFile, basePath: String): SynchronizableFile {
            return SynchronizableFile(
                getName(file.remotePath ?: ""),
                relativePath(basePath, file.remotePath ?: ""),
                file.remotePath!!,
                file.size,
                file.modifiedTimestamp,
                file.mimeType == "DIR"
            )
        }

        fun from(file: File, basePath: String): SynchronizableFile {
            return SynchronizableFile(
                file.name,
                relativePath(basePath, file.path),
                file.absolutePath,
                file.length(),
                file.lastModified(),
                file.isDirectory
            )
        }

        private fun getName(file: String): String {
            return file.split("/").last { x -> x.isNotEmpty() }
        }

        private fun relativePath(basePath: String, path: String): String {
            return path.removePrefix(basePath)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false

        if (other is SynchronizableFile) {
            return other.size == size && other.edited == edited
        }

        return false
    }
}