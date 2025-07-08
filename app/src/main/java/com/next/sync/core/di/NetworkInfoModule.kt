package com.next.sync.core.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.next.sync.core.model.NetworkInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class NetworkInfoModule @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val networkInfo: Flow<NetworkInfo> = callbackFlow {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun updateNetworkInfo() {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            val isConnectedWifi =
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            val isConnectedMobile =
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
            val isConnected = isConnectedWifi || isConnectedMobile

            trySend(
                NetworkInfo(
                    isConnected = isConnected,
                    isConnectedWifi = isConnectedWifi,
                    isConnectedMobile = isConnectedMobile
                )
            ).isSuccess
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = updateNetworkInfo()
            override fun onLost(network: Network) = updateNetworkInfo()
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) = updateNetworkInfo()
        }

        updateNetworkInfo()

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}