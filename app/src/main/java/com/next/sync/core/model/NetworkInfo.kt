package com.next.sync.core.model

data class NetworkInfo(
    val isConnected: Boolean,
    val isConnectedWifi: Boolean,
    val isConnectedMobile: Boolean
)
