package com.next.sync.ui.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.next.sync.core.di.BatteryInfoModule
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
    private val notificationModule: NotificationModule
) : EventViewModel<HomeEvents>() {

    override val events: Map<String, (HomeEvents) -> Unit> = mapOf(
        forEvent<HomeEvents.SynchronizeNow> { synchronize() }
    )

    var homeState by mutableStateOf(HomeState())

    fun launch() {
        viewModelScope.launch(Dispatchers.IO) {
            batteryInfoModule.batteryInfo.collect { info ->
                homeState = homeState.copy(isBatteryCharging = info.isCharging)
            }
        }

        viewModelScope.launch {
            networkInfoModule.networkInfo.collect { info ->
                Log.d("NetworkViewModel", "Network launch info: $info")
                homeState = homeState.copy(
                    isUsingWifi = info.isConnectedWifi,
                    isUsingMobileData = info.isConnectedMobile,
                    isConnectedToNetwork = info.isConnected
                )
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
                storageUsed = quota?.used ?: 0,
                storageTotal = quota?.total ?: 0
            )
        } catch (e: Exception){
            Log.d("HomeViewModel", "getQuota: ${e.message}")

        }
    }

    private fun synchronize() {
        viewModelScope.launch(Dispatchers.IO) {
            synchronizationModule.sync()
        }
    }
}