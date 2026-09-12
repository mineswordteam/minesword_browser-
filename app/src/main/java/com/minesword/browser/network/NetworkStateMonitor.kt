package com.minesword.browser.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NetworkStatus {
    ONLINE,             // Full internet connectivity
    LOCAL_NETWORK_ONLY, // Connected to local network (WiFi/Ethernet) but no internet
    OFFLINE             // No network connection at all
}

class NetworkStateMonitor(context: Context) {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkStatus = MutableStateFlow(getInitialStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateNetworkStatus()
        }

        override fun onLost(network: Network) {
            updateNetworkStatus()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            updateNetworkStatus()
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    fun updateNetworkStatus() {
        _networkStatus.value = getCurrentStatus()
    }

    private fun getInitialStatus(): NetworkStatus {
        return getCurrentStatus()
    }

    fun getCurrentStatus(): NetworkStatus {
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkStatus.OFFLINE
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkStatus.OFFLINE

        val hasInternetCapability = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val hasValidatedInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        return when {
            hasInternetCapability && hasValidatedInternet -> NetworkStatus.ONLINE
            hasInternetCapability -> NetworkStatus.LOCAL_NETWORK_ONLY
            else -> NetworkStatus.OFFLINE
        }
    }
}
