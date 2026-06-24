package com.next.sync.ui.components.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.next.sync.R
import com.next.sync.core.sync.model.Progress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ProgressNotification(
    val context: Context, val progress: Flow<Progress?>
) {
    val channelId = "NextSync_transfer"
    val channelName = "File transfer progress"

    val notificationManager =
        context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    lateinit var notificationChannel: NotificationChannel
    lateinit var notificationBuilder: NotificationCompat.Builder

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationBuilder = NotificationCompat.Builder(context, channelId)
        notificationBuilder
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.color.ic_launcher_background)
            .setContentTitle("Uploading in progress...").setProgress(100, 0, true)

        notificationManager.notify(42, notificationBuilder.build())

        job = scope.launch {
            progress.collect { value ->
                if (value != null) {
                    val percent = value.total / 100
                    val progressInt = if (percent > 0) (value.done / percent).toInt() else 0
                    notificationBuilder
                        .setContentText(value.fileName)
                        .setProgress(100, progressInt, false)
                    notificationManager.notify(42, notificationBuilder.build())

                    if (progressInt >= 100) {
                        finish()
                        return@collect
                    }
                }
            }

            finish()
        }
    }

    fun cancel() {
        job?.cancel()
        notificationManager.cancel(42)
    }

    private fun finish() {
        notificationBuilder
            .setOngoing(false)
            .setContentTitle("Done")
        notificationManager.notify(42, notificationBuilder.build())
    }
}