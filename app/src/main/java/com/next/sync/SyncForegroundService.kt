package com.next.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.next.sync.core.di.DataBus
import com.next.sync.core.di.DataBusKey
import com.next.sync.core.sync.SyncProgressTracker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SyncForegroundService : Service() {

    @Inject lateinit var progressTracker: SyncProgressTracker
    @Inject lateinit var dataBus: DataBus

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var collectionJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SYNC) {
            dataBus.emit(DataBusKey.SyncStop, Unit)
            return START_NOT_STICKY
        }

        val notification = buildNotification(
            title = "Syncing...",
            body = "Preparing",
            progress = 0,
            max = 0,
            indeterminate = true,
            ongoing = true
        )
        startForeground(NOTIFICATION_ID, notification)

        collectionJob?.cancel()
        collectionJob = serviceScope.launch {
            progressTracker.state.collect { state ->
                if (!state.isRunning) {
                    val finalNotification = buildFinalNotification(state)
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.notify(NOTIFICATION_ID, finalNotification)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@collect
                }
                updateNotification(state)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        collectionJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private var lastNotificationBody: String = ""

    private fun updateNotification(state: com.next.sync.core.sync.model.SyncProgressState) {
        val progressMax = if (state.bytesTotal > 0) 100 else 0
        val progressInt = if (state.bytesTotal > 0)
            ((state.bytesDone * 100L) / state.bytesTotal).toInt() else 0

        val body = buildString {
            append("${state.filesDone} / ${state.filesTotal} files")
            if (state.speedBytesPerSec > 0) {
                append(" · ${formatSpeed(state.speedBytesPerSec)}")
            }
            append("\n${formatBytes(state.bytesDone)} / ${formatBytes(state.bytesTotal)}")
            if (state.estimatedTimeLeftMs > 0) {
                append(" · ~${formatDuration(state.estimatedTimeLeftMs)} left")
            }
        }

        if (body == lastNotificationBody) return
        lastNotificationBody = body

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(state.currentFile.ifEmpty { "Syncing..." })
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, progressInt, progressMax == 0)
            .addAction(0, "Stop", stopPendingIntent)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun buildFinalNotification(state: com.next.sync.core.sync.model.SyncProgressState): Notification {
        val summaryBody = if (state.errors.isEmpty()) {
            "Completed: ${state.filesDone} files, ${formatBytes(state.bytesDone)}"
        } else {
            "Completed with ${state.errors.size} error(s)"
        }
        return buildNotification(
            title = "Sync finished",
            body = summaryBody,
            progress = 0,
            max = 0,
            indeterminate = false,
            ongoing = false
        )
    }

    private fun buildNotification(
        title: String,
        body: String,
        progress: Int,
        max: Int,
        indeterminate: Boolean,
        ongoing: Boolean
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
            .setProgress(max, progress, indeterminate)
            .setAutoCancel(!ongoing)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private val stopPendingIntent: PendingIntent by lazy {
        val intent = Intent(this, SyncForegroundService::class.java).apply {
            action = ACTION_STOP_SYNC
        }
        PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1_000_000 -> "${bytesPerSec / 1_000_000} MB/s"
            bytesPerSec >= 1_000 -> "${bytesPerSec / 1_000} KB/s"
            else -> "$bytesPerSec B/s"
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> "${"%.1f".format(bytes / 1_000_000_000.0)} GB"
            bytes >= 1_000_000 -> "${"%.1f".format(bytes / 1_000_000.0)} MB"
            bytes >= 1_000 -> "${"%.1f".format(bytes / 1_000.0)} KB"
            else -> "$bytes B"
        }
    }

    private fun formatDuration(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return if (min > 0) "${min}m ${sec}s" else "${sec}s"
    }

    companion object {
        private const val NOTIFICATION_ID = 42
        private const val CHANNEL_ID = "sync_progress"
        private const val CHANNEL_NAME = "Sync Progress"
        const val ACTION_STOP_SYNC = "com.next.sync.STOP_SYNC"
    }
}
