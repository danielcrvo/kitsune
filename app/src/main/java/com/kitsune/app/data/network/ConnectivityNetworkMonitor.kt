package com.kitsune.app.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.kitsune.app.domain.model.NetworkStatus
import com.kitsune.app.domain.repository.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityNetworkMonitor @Inject constructor(
    @ApplicationContext context: Context
) : NetworkMonitor {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    override fun isWifiConnected(): Boolean = currentStatus() == NetworkStatus.WIFI

    override val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val cm = connectivityManager
        if (cm == null) {
            trySend(NetworkStatus.DISCONNECTED)
            close()
            return@callbackFlow
        }

        trySend(currentStatus())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(currentStatus())
            }

            override fun onLost(network: Network) {
                trySend(currentStatus())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(currentStatus())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, callback)

        awaitClose {
            cm.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    private fun currentStatus(): NetworkStatus {
        val cm = connectivityManager ?: return NetworkStatus.DISCONNECTED
        val active = cm.activeNetwork ?: return NetworkStatus.DISCONNECTED
        val caps = cm.getNetworkCapabilities(active) ?: return NetworkStatus.DISCONNECTED
        return when {
            !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetworkStatus.DISCONNECTED
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.CELLULAR
            else -> NetworkStatus.WIFI
        }
    }
}
