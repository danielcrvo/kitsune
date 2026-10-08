package com.kitsune.app.domain.repository

import com.kitsune.app.domain.model.NetworkStatus
import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val networkStatus: Flow<NetworkStatus>
    fun isWifiConnected(): Boolean
}
