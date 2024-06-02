package com.next.sync.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.next.sync.core.di.BatteryInfoModule
import com.next.sync.core.di.NextcloudClientHelper
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
    private val batteryInfoModule: BatteryInfoModule
) : ViewModel() {

    var homeState by mutableStateOf(HomeState())

    fun launch() {
        viewModelScope.launch(Dispatchers.IO) {
            batteryInfoModule.batteryInfo.collect { info ->
                homeState = homeState.copy(isBatteryCharging = info.isCharging)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            getQuota()
        }
    }

    private suspend fun getQuota() {
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
    }

    fun onEvent(event: HomeEvents) {
//        when (event) {
//            HomeEvents.SynchronizeNow -> {}
//        }
    }
}