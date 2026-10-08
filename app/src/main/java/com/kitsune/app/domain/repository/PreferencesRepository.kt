package com.kitsune.app.domain.repository

import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.model.DownloadMode
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val downloadConfig: Flow<DownloadConfig>
    val downloadMode: Flow<DownloadMode>
    val isAmoledTheme: Flow<Boolean>
    val isDynamicColor: Flow<Boolean>
    val isWifiOnly: Flow<Boolean>
    val isAutoCheckUpdates: Flow<Boolean>

    suspend fun saveDownloadConfig(config: DownloadConfig)
    suspend fun saveDownloadMode(mode: DownloadMode)
    suspend fun setAmoledTheme(enabled: Boolean)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setWifiOnly(enabled: Boolean)
    suspend fun setAutoCheckUpdates(enabled: Boolean)
}
