package com.next.sync.ui.home

import android.app.Notification
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewModelScope
import com.next.sync.R
import com.next.sync.core.di.BatteryInfoModule
import com.next.sync.core.di.DataBus
import com.next.sync.core.di.DataBusKey
import com.next.sync.core.di.NetworkInfoModule
import com.next.sync.core.di.NextcloudClientHelper
import com.next.sync.core.di.NotificationModule
import com.next.sync.core.di.SynchronizationModule
import com.next.sync.ui.EventViewModel
import com.next.sync.ui.events.HomeEvents
import com.owncloud.android.lib.common.UserInfo
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.users.GetUserInfoRemoteOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


data class HomeState(
    val isSynchronizing: Boolean = false,
    val isUsingWifi: Boolean = false,
    val isUsingMobileData: Boolean = false,
    val isConnectedToNetwork: Boolean = false,
    val isBatteryCharging: Boolean = false,
    val lastSync: String = "",
    val nextSync: String = "",
    val allTimeUpload: String = "",
    val allTimeDownload: String = "",
    val storageUsed: Long = 0,
    val storageTotal: Long = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val nextcloudClientHelper: NextcloudClientHelper,
    private val batteryInfoModule: BatteryInfoModule,
    private val networkInfoModule: NetworkInfoModule,
    private val synchronizationModule: SynchronizationModule,
    private val dataBus: DataBus,
    private val notificationModule: NotificationModule,
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManagerCompat
) : EventViewModel<HomeEvents>() {

    override val events: Map<String, (HomeEvents) -> Unit> = mapOf(
        forEvent<HomeEvents.SynchronizeNow> { synchronize() })

    var homeState by mutableStateOf(HomeState())

    private var syncPending = false

    fun launch() {
        viewModelScope.launch(Dispatchers.IO) {
            batteryInfoModule.batteryInfo.collect { info ->
                homeState = homeState.copy(isBatteryCharging = info.isCharging)
            }
        }

        viewModelScope.launch {
            var wasConnected = false
            networkInfoModule.networkInfo.collect { info ->
                val isConnected = info.isConnected
                Log.d("NetworkViewModel", "Network launch info: $info")
                homeState = homeState.copy(
                    isUsingWifi = info.isConnectedWifi,
                    isUsingMobileData = info.isConnectedMobile,
                    isConnectedToNetwork = isConnected
                )
                if (isConnected && !wasConnected) {
                    launch(Dispatchers.IO) { getQuota() }
                    retryPendingSync()
                }
                wasConnected = isConnected
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            getQuota()
        }
    }

    private suspend fun getQuota() {
        try {
            var client = nextcloudClientHelper.client

            if (client == null) {
                nextcloudClientHelper.loadService()
                client = nextcloudClientHelper.client ?: return
            }

            val result: RemoteOperationResult<UserInfo> =
                GetUserInfoRemoteOperation().execute(client)

            val quota = result.resultData.quota
            homeState = homeState.copy(
                storageUsed = quota?.used ?: 0, storageTotal = quota?.total ?: 0
            )
        } catch (e: Exception) {
            Log.d("HomeViewModel", "getQuota: ${e.message}")

        }
    }

    private fun synchronize() {
        val notificationId = 1
        dataBus.register(DataBusKey.ProgressFlowReset) { progress ->
            if (progress is com.next.sync.core.sync.model.Progress) {
                val notification = buildProgressNotification(
                    context, progress.done.toInt(), progress.total.toInt(), progress.fileName
                )
                notificationManager.notify(notificationId, notification)
                Log.d("ViewModel", "Progress: ${(progress.done / progress.total)} ${progress.done}")
            }
        }
        syncPending = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                synchronizationModule.sync()
                syncPending = false
            } catch (e: Exception) {
                Log.d("HomeViewModel", "synchronize: ${e.message}")
            }
        }
    }

    private fun retryPendingSync() {
        if (!syncPending) return
        syncPending = false
        viewModelScope.launch(Dispatchers.IO) {
            try {
                synchronizationModule.sync()
            } catch (e: Exception) {
                Log.d("HomeViewModel", "retrySync: ${e.message}")
                syncPending = true
            }
        }
    }


    fun buildProgressNotification(
        context: Context, progress: Int, max: Int, title: String
    ): Notification {
        return NotificationCompat.Builder(context, "Main Channel ID")
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(title)
            .setOngoing(true)
            .setContentText("Upload progress: ${(progress.toFloat() / max.toFloat()).times(100).toInt()}%")
            .setProgress(max, progress, false)
            .build()
    }
}