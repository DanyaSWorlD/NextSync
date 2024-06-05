package com.next.sync.core.wokers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.next.sync.core.db.ObjectBox.store
import com.next.sync.core.db.data.DirectoryEntity
import com.next.sync.core.db.data.DirectoryEntity_
import io.objectbox.kotlin.boxFor
import java.io.File

class SyncCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params){
    override suspend fun doWork(): Result {
        Log.d("DirScanner", "Started")

        val data = inputData
        val path = data.getString("path") ?: return Result.failure()
        val directory = File(path)

        if(!directory.exists() || !directory.isDirectory )
            return Result.failure()

        val previousState = previousState(path)
        val currentState = currentState(directory)

        if (previousState == null){
            saveDirectoryState(currentState)
            return Result.success()
        }

        if (isDirContentChanged(previousState.fileMap, currentState.fileMap))
            saveDirectoryState(currentState)

        return Result.success()
    }
}

fun previousState(path: String): DirectoryEntity? {
    val directory = store.boxFor(DirectoryEntity::class)
    val query = directory.query(
        DirectoryEntity_.directoryPath.equal(path)
    ).build()

    val results = query.find()
    query.close()

    if (results.isNotEmpty()) return results.first()
    Log.d("DirScanner", "Got null")

    return null
}

fun currentState(directory: File): DirectoryEntity {
    val fileMap = directory.listFiles()?.associate { it.name to it.lastModified() } ?: emptyMap()

    return DirectoryEntity( fileMap = fileMap, directoryPath = directory.absolutePath)
}

fun isDirContentChanged(previous: Map<String, Long>, current: Map<String, Long>): Boolean {
    val added = current.keys - previous.keys
    val removed = previous.keys - current.keys
    val modified = current.filter { (key, value) -> previous[key] != value }.keys

    if (added.isNotEmpty() || removed.isNotEmpty() || modified.isNotEmpty()){
        Log.d("DirScanner", "Added: $added")
        Log.d("DirScanner", "Removed: $removed")
        Log.d("DirScanner", "Modified: $modified")
        return true
    }else
        Log.d("DirScanner", "No change")

    return false
}

fun saveDirectoryState(directory: DirectoryEntity) {
    Log.d("DirScanner", "Saving")
    val directoryBox = store.boxFor(DirectoryEntity::class)
    directoryBox.put(directory)
    Log.d("DirScanner", "Saved")
}