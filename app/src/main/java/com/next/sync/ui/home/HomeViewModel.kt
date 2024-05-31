package com.next.sync.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.next.sync.core.di.AccountService
import com.next.sync.ui.events.HomeEvents
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
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
    val storageUsed: Int = 0,
    val storageTotal: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    var homeState by mutableStateOf(HomeState())

    fun onEvent(event: HomeEvents) {
        when (event) {
            HomeEvents.SynchronizeNow -> {
                CoroutineScope(Dispatchers.IO).launch {
                    accountService.getCurrentAccountId().collect { id ->
                        val data = accountService.getAccountData(id)

                        val sardine = OkHttpSardine()
                        sardine.setCredentials(data?.user, data?.password)

                        val server = data?.server
                        val account = data?.user

                        val resources = sardine.getQuota("$server/remote.php/dav/files/$account/")
                        if(resources == null) return@collect
                    }
                }
            }
        }
    }
}