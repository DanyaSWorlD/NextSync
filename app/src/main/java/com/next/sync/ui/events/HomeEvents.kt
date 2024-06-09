package com.next.sync.ui.events

sealed class HomeEvents {
    data object SynchronizeNow : HomeEvents()
}