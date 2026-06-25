package com.next.sync.core.sync

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.next.sync.SyncForegroundService
import com.next.sync.core.sync.model.SyncProgressState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncProgressTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManagerCompat
) {
    private val _state = MutableStateFlow(SyncProgressState())
    val state: StateFlow<SyncProgressState> = _state.asStateFlow()

    private var completedBytes: Long = 0L
    private var startTimeElapsed: Long = 0L
    private var lastCompletedFile: String = ""
    private var lastStateEmitMs: Long = 0L

    companion object {
        private const val STATE_EMIT_INTERVAL_MS = 150L
    }

    fun start(filesTotal: Int, bytesTotal: Long) {
        completedBytes = 0L
        startTimeElapsed = SystemClock.elapsedRealtime()
        lastCompletedFile = ""
        lastStateEmitMs = 0L

        _state.value = SyncProgressState(
            isRunning = true,
            filesTotal = filesTotal,
            bytesTotal = bytesTotal,
            currentFile = "Scanning..."
        )

        startForegroundService()
    }

    fun updateTotals(filesTotal: Int, bytesTotal: Long) {
        _state.value = _state.value.copy(
            filesTotal = filesTotal,
            bytesTotal = bytesTotal,
            currentFile = ""
        )
    }

    fun onFileProgress(instantRate: Long, fileDone: Long, fileTotal: Long, fileName: String) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastStateEmitMs < STATE_EMIT_INTERVAL_MS) return
        lastStateEmitMs = now

        val current = _state.value
        if (!current.isRunning) return

        val aggregateBytes = completedBytes + fileDone
        pushState(aggregateBytes, instantRate, fileName, now)
    }

    fun onFileComplete(fileName: String, fileBytes: Long) {
        if (fileName == lastCompletedFile) return
        lastCompletedFile = fileName

        completedBytes += fileBytes
        val current = _state.value
        _state.value = current.copy(
            filesDone = current.filesDone + 1,
            bytesDone = completedBytes,
            currentFile = fileName
        )
    }

    fun onError(message: String) {
        val current = _state.value
        _state.value = current.copy(errors = current.errors + message)
    }

    fun cancel() {
        val current = _state.value
        _state.value = current.copy(
            isRunning = false,
            estimatedTimeLeftMs = 0L,
            speedBytesPerSec = 0L,
            currentFile = ""
        )

        stopForegroundService()
    }

    fun finish() {
        val current = _state.value
        _state.value = current.copy(
            isRunning = false,
            estimatedTimeLeftMs = 0L,
            speedBytesPerSec = 0L,
            currentFile = ""
        )

        stopForegroundService()
    }

    private fun pushState(aggregateBytes: Long, instantRate: Long, fileName: String, now: Long) {
        val elapsed = now - startTimeElapsed
        val avgSpeed = if (elapsed > 0) (aggregateBytes * 1000L) / elapsed else instantRate
        val remaining = _state.value.bytesTotal - aggregateBytes
        val etaMs = if (avgSpeed > 0) (remaining * 1000L) / avgSpeed else 0L

        _state.value = _state.value.copy(
            bytesDone = aggregateBytes,
            speedBytesPerSec = avgSpeed,
            estimatedTimeLeftMs = etaMs,
            currentFile = fileName
        )
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startForegroundService() {
        if (!hasNotificationPermission()) return
        val intent = Intent(context, SyncForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopForegroundService() {
        val intent = Intent(context, SyncForegroundService::class.java)
        context.stopService(intent)
    }
}
