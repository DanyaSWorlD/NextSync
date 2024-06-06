package com.next.sync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.next.sync.background.wokers.SyncCheckWorker
import com.next.sync.ui.AppNavigation
import com.next.sync.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWorkers()
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun initWorkers() {
        val downloadPath = getDownloadsDirectory().absolutePath
        val inputData = workDataOf("path" to downloadPath)

        val localSyncWorker = PeriodicWorkRequestBuilder<SyncCheckWorker>(15, TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DirectoryScanWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            localSyncWorker
        )
    }
}