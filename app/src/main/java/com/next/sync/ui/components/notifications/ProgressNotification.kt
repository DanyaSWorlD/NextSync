package com.next.sync.ui.components.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.next.sync.R
import com.next.sync.core.sync.model.Progress
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

class ProgressNotification(
    val context: Context, val progress: Flow<Progress?>
) {
    val channelId = "NextSync_transfer"
    val channelName = "File transfer progress"

    val notificationManager =
        context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    lateinit var notificationChannel: NotificationChannel
    lateinit var notificationBuilder: NotificationCompat.Builder

    fun show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationBuilder = NotificationCompat.Builder(context, channelId)
        notificationBuilder
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Uploading in progress...").setProgress(100, 0, true)

        notificationManager.notify(42, notificationBuilder.build())

        runBlocking {
            progress.collect {
                if (it != null) {
                    val percent = it.total / 100
                    val progressInt = (it.done / percent).toInt()
                    notificationBuilder
                        .setContentText(it.fileName)
                        .setProgress(100, progressInt, false)
                    notificationManager.notify(42, notificationBuilder.build())

                    if (progressInt == 100) this.cancel()
                }
            }

            notificationBuilder
                .setOngoing(false)
                .setContentTitle("Done")

            notificationManager.notify(42, notificationBuilder.build())
        }
    }
}