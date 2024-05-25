package com.next.sync.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.next.sync.ui.events.HomeEvents


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

class HomeViewModel : ViewModel() {

    var homeState by mutableStateOf(HomeState())

    fun onEvent(event: HomeEvents) {
        when (event) {
            HomeEvents.SynchronizeNow -> {}
        }
    }
}