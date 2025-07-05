package com.next.sync.core.sync

import com.next.sync.core.sync.model.Progress
import com.next.sync.core.sync.model.SynchronizableFile
import com.next.sync.core.sync.strategy.ISyncStrategy
import com.next.sync.core.sync.tasks.DownloadTask
import com.next.sync.core.sync.tasks.ILongRunningSyncTask
import com.next.sync.core.sync.tasks.ISyncTask
import com.next.sync.core.sync.tasks.UploadTask
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class SyncWorker(
    val localPath: String,
    val remotePath: String,
    val strategy: ISyncStrategy,
    val client: OwnCloudClient
) {
    private val _progress = MutableStateFlow<Progress?>(null)
    val progressFlow: StateFlow<Progress?> = _progress
    
    private val _errors = MutableStateFlow<List<SyncError>>(emptyList())
    val errorsFlow: StateFlow<List<SyncError>> = _errors
    
    private val _stats = MutableStateFlow(SyncStats())
    val statsFlow: StateFlow<SyncStats> = _stats
    
    private val processedFiles = ConcurrentHashMap<String, Boolean>()
    private val maxRetries = 3
    
    fun sync(relativePath: String = "") {
        try {
            val localFiles = getLocalFiles(relativePath)
            val remoteFiles = getRemoteFiles(relativePath)
            
            processFiles(localFiles, remoteFiles)
            
            // Update stats
            _stats.value = _stats.value.copy(
                filesProcessed = processedFiles.size,
                syncComplete = true
            )
        } catch (e: Exception) {
            addError(SyncError(relativePath, e.message ?: "Unknown error", e))
        }
    }
    
    private fun processFiles(
        localFiles: Map<String, SynchronizableFile>,
        remoteFiles: Map<String, SynchronizableFile>
    ) {
        val allPaths = (localFiles.keys + remoteFiles.keys).distinct()
        
        for (path in allPaths) {
            if (processedFiles.containsKey(path)) continue
            
            val localFile = localFiles[path]
            val remoteFile = remoteFiles[path]
            
            var retryCount = 0
            var success = false
            
            while (!success && retryCount < maxRetries) {
                try {
                    val task = strategy.decide(localFile, remoteFile)
                    if (task != null) {
                        executeTask(task)
                    }
                    success = true
                    processedFiles[path] = true
                } catch (e: Exception) {
                    retryCount++
                    if (retryCount >= maxRetries) {
                        addError(SyncError(path, "Failed after $maxRetries retries: ${e.message}", e))
                    }
                }
            }
        }
    }
    
    private fun executeTask(task: ISyncTask) {
        var current: ISyncTask = task
        while (true) {
            notifyProgress(current)
            current.run(client)
            updateStats(current)
            
            if (!current.hasNext) break
            current = current.next()
        }
    }
    
    private fun getLocalFiles(relativePath: String): Map<String, SynchronizableFile> {
        val baseDir = File(localPath)
        val targetDir = if (relativePath.isEmpty()) baseDir else File(baseDir, relativePath)
        
        return targetDir.listFiles()?.map { file ->
            val relPath = if (relativePath.isEmpty()) file.name else "$relativePath/${file.name}"
            relPath to SynchronizableFile.from(file, localPath)
        }?.toMap() ?: emptyMap()
    }
    
    private fun getRemoteFiles(relativePath: String): Map<String, SynchronizableFile> {
        val fullPath = if (relativePath.isEmpty()) remotePath else "$remotePath/$relativePath"
        val result = ReadFolderRemoteOperation(fullPath).execute(client)
        
        if (!result.isSuccess) {
            throw Exception("Failed to read remote directory: ${result.message}")
        }
        
        val response = result.data as List<RemoteFile>
        return response.subList(1, response.size).mapNotNull { file ->
            val relPath = if (relativePath.isEmpty()) file.remotePath else "$relativePath/${file.remotePath}"
            relPath?.let { path ->
                path to SynchronizableFile.from(file, remotePath)
            }
        }.toMap()
    }
    
    private fun notifyProgress(task: ISyncTask) {
        if (task is ILongRunningSyncTask) {
            val progressFlow = task.progressFlow
            if (progressFlow is StateFlow<Progress?>) {
                _progress.value = progressFlow.value
            }
        }
    }
    
    private fun updateStats(task: ISyncTask) {
        val currentStats = _stats.value
        _stats.value = when (task) {
            is UploadTask -> currentStats.copy(
                bytesUploaded = currentStats.bytesUploaded
            )
            is DownloadTask -> currentStats.copy(
                bytesDownloaded = currentStats.bytesDownloaded
            )
            else -> currentStats
        }
    }
    
    private fun addError(error: SyncError) {
        _errors.value = _errors.value + error
    }
    
    data class SyncError(
        val path: String,
        val message: String,
        val exception: Exception? = null
    )
    
    data class SyncStats(
        val filesProcessed: Int = 0,
        val bytesUploaded: Long = 0,
        val bytesDownloaded: Long = 0,
        val syncComplete: Boolean = false
    )
}