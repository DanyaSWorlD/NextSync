package com.next.sync.domain

import android.os.Build
import android.os.FileObserver
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File

@RequiresApi(Build.VERSION_CODES.Q)
class PathObserver(path: String) : FileObserver(File(path), ALL_EVENTS) {
    override fun onEvent(event: Int, path: String?) {
        when (event) {
            CREATE -> {
                Log.d("Monitoring","File created: $path")
            }

            DELETE -> {
                Log.d("Monitoring","File deleted: $path")
            }

            MODIFY -> {
                Log.d("Monitoring","File modified: $path")
            }

            MOVED_FROM -> {
                Log.d("Monitoring","File moved from: $path")
            }

            MOVED_TO -> {
                Log.d("Monitoring","File moved to: $path")
            }

            OPEN -> {
                Log.d("Monitoring","File opened: $path")
            }
        }
    }
}