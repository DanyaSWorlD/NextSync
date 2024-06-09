package com.next.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.next.sync.domain.PathObserver
import java.io.File

class MonitoringService: Service() {
    private lateinit var pathObserver: PathObserver

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        val pathToWatch =  getDownloadsDirectory().absolutePath
        pathObserver = PathObserver(pathToWatch)
        startForegroundService()
        pathObserver.startWatching()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        pathObserver.stopWatching()
    }

    private fun startForegroundService() {
        val channelId = "directory_monitor_channel"
        val channelName = "Directory Monitor"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Directory Monitoring Service")
            .setContentText("Monitoring directory changes")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }
}

fun getDownloadsDirectory(): File {
    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
}