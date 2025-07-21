package com.next.sync.core.sync

import android.util.Log
import com.next.sync.core.sync.model.Progress
import com.next.sync.core.sync.model.SynchronizableFile
import com.next.sync.core.sync.strategy.ISyncStrategy
import com.next.sync.core.sync.tasks.ISyncTask
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class NextSync(
    private val client: OwnCloudClient
) {
    var maxRetries = 3
    private val processedFiles = ConcurrentHashMap<String, Boolean>()


    suspend fun sync(
        localPath: String,
        remotePath: String,
        strategy: ISyncStrategy,
        callback: (Progress) -> Unit,
    ) {
//        var worker = SyncWorker(localPath, remotePath, strategy, client)
        //if (callback != null)
        //worker.progressFlow
//        worker.sync()

        try {
            val localFiles = getLocalFiles("", localPath)
            val remoteFiles = getRemoteFiles("", remotePath)

            processFiles(localFiles, remoteFiles, strategy, callback)
        } catch (e: Exception) {
            throw Exception("Failed to sync: ${e.message}")
        }

    }

    suspend fun getLocalFiles(relativePath: String, localPath: String): Map<String, SynchronizableFile> {
        val baseDir = File(localPath)
        val targetDir = if (relativePath.isEmpty()) baseDir else File(baseDir, relativePath)

        return targetDir.listFiles()?.associate { file ->
            val relPath = if (relativePath.isEmpty()) file.name else "$relativePath/${file.name}"
            relPath to SynchronizableFile.from(file, localPath)
        } ?: emptyMap()
    }

    suspend fun getRemoteFiles(relativePath: String, remotePath: String): Map<String, SynchronizableFile> {
        val fullPath = if (relativePath.isEmpty()) remotePath else "$remotePath/$relativePath"
        Log.d("NextSync", "getRemoteFiles: $fullPath")
        val result = ReadFolderRemoteOperation(fullPath).execute(client)

        if (!result.isSuccess) {
            throw Exception("Failed to read remote directory: ${result.message}")
        }

        val response = result.data as List<RemoteFile>
        return response.subList(1, response.size).mapNotNull { file ->
            val relPath =
                if (relativePath.isEmpty()) file.remotePath else "$relativePath/${file.remotePath}"
            relPath?.let { path ->
                path to SynchronizableFile.from(file, remotePath)
            }
        }.toMap()
    }

    suspend fun processFiles(
        localFiles: Map<String, SynchronizableFile>,
        remoteFiles: Map<String, SynchronizableFile>,
        strategy: ISyncStrategy,
        progress: (Progress) -> Unit
    ) {

        val allPaths = (localFiles.keys + remoteFiles.keys).distinct()

        for (path in allPaths) {
            val localFile = localFiles[path]
            val remoteFile = remoteFiles[path]

            var retryCount = 0
            var success = false

            while (!success && retryCount < maxRetries) {
                try {
                    val task = strategy.decide(localFile, remoteFile)
                    if (task != null) {
                        executeTask(task, progress)
                    }
                    success = true
                    processedFiles[path] = true
                } catch (e: Exception) {
                    retryCount++
                    if (retryCount >= maxRetries) {
                        throw Exception("Failed after $maxRetries retries: ${e.message}")
                    }
                }
            }

        }


    }

    fun executeTask(task: ISyncTask, progress: (Progress) -> Unit) {
        var current: ISyncTask = task

        while (true) {
            current.run(client, progress)
            if (!current.hasNext) break
            current = current.next()
        }
    }
}