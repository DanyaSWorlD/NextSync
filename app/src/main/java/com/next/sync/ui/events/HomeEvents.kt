package com.next.sync.ui.events

sealed class HomeEvents {
    data object SynchronizeNow : HomeEvents()
    data object StopSync : HomeEvents()
    data object CheckBatteryOptimization : HomeEvents()
    data class DismissRun(val id: Long) : HomeEvents()
}