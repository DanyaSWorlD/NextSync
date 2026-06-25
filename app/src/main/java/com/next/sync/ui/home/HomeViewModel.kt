package com.next.sync.ui.home

import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.next.sync.core.di.BatteryInfoModule
import com.next.sync.core.di.DataBus
import com.next.sync.core.di.DataBusKey
import com.next.sync.core.di.NetworkInfoModule
import com.next.sync.core.di.NextcloudClientHelper
import com.next.sync.core.di.SynchronizationModule
import com.next.sync.core.sync.SyncProgressTracker
import com.next.sync.core.sync.model.SyncProgressState
import com.next.sync.core.sync.model.SyncRunRecord
import com.next.sync.ui.EventViewModel
import com.next.sync.ui.events.HomeEvents
import com.owncloud.android.lib.common.UserInfo
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.users.GetUserInfoRemoteOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject


data class HomeState(
    val isSynchronizing: Boolean = false,
    val isUsingWifi: Boolean = false,
    val isUsingMobileData: Boolean = false,
    val isConnectedToNetwork: Boolean = false,
    val isBatteryCharging: Boolean = false,
    val isBatteryOptimizationExempt: Boolean = false,
    val lastSync: String = "",
    val nextSync: String = "",
    val allTimeUpload: String = "",
    val allTimeDownload: String = "",
    val storageUsed: Long = 0,
    val storageTotal: Long = 0,
    val syncProgress: SyncProgressState = SyncProgressState(),
    val syncHistory: List<SyncRunRecord> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val nextcloudClientHelper: NextcloudClientHelper,
    private val batteryInfoModule: BatteryInfoModule,
    private val networkInfoModule: NetworkInfoModule,
    private val synchronizationModule: SynchronizationModule,
    private val dataBus: DataBus,
    private val progressTracker: SyncProgressTracker,
    @ApplicationContext private val context: android.content.Context,
) : EventViewModel<HomeEvents>() {

    override val events: Map<String, (HomeEvents) -> Unit> = mapOf(
        forEvent<HomeEvents.SynchronizeNow> { synchronize() },
        forEvent<HomeEvents.StopSync> { stopSync() },
        forEvent<HomeEvents.CheckBatteryOptimization> { checkBatteryOptimization() },
        forEvent<HomeEvents.DismissRun> { dismissRun(it.id) })

    var homeState by mutableStateOf(HomeState())
    private var syncJob: Job? = null
    private var wasRunning = false
    private var syncStartTimeMs = 0L
    private var nextRunId = 0L

    private val stopSyncListener: (Any?) -> Unit = { stopSync() }

    fun launch() {
        dataBus.register(DataBusKey.SyncStop, stopSyncListener)
        checkBatteryOptimization()

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

        viewModelScope.launch {
            progressTracker.state.collect { progress ->
                if (!wasRunning && progress.isRunning) {
                    syncStartTimeMs = System.currentTimeMillis()
                } else if (wasRunning && !progress.isRunning) {
                    val elapsed = if (syncStartTimeMs > 0)
                        System.currentTimeMillis() - syncStartTimeMs else 0L
                    homeState = homeState.copy(
                        syncHistory = (listOf(
                            SyncRunRecord(
                                id = nextRunId++,
                                timestamp = System.currentTimeMillis(),
                                filesTotal = progress.filesTotal,
                                filesDone = progress.filesDone,
                                bytesTotal = progress.bytesTotal,
                                bytesDone = progress.bytesDone,
                                durationMs = elapsed,
                                errors = progress.errors
                            )
                        ) + homeState.syncHistory).take(5)
                    )
                }
                wasRunning = progress.isRunning
                homeState = homeState.copy(
                    isSynchronizing = progress.isRunning,
                    syncProgress = progress
                )
            }
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

    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager
            homeState = homeState.copy(
                isBatteryOptimizationExempt = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            )
        } else {
            homeState = homeState.copy(isBatteryOptimizationExempt = true)
        }
    }

    private fun synchronize() {
        homeState = homeState.copy(isSynchronizing = true)
        syncJob?.cancel()
        syncJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                synchronizationModule.sync()
            } catch (e: CancellationException) {
                progressTracker.cancel()
            } catch (e: Exception) {
                Log.d("HomeViewModel", "synchronize: ${e.message}")
            }
        }
    }

    private fun stopSync() {
        syncJob?.cancel()
        syncJob = null
    }

    private fun dismissRun(id: Long) {
        homeState = homeState.copy(syncHistory = homeState.syncHistory.filter { it.id != id })
    }

    override fun onCleared() {
        super.onCleared()
        dataBus.unregister(DataBusKey.SyncStop, stopSyncListener)
    }
}
