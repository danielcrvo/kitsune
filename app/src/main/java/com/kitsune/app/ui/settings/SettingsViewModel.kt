package com.kitsune.app.ui.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitsune.app.R
import com.kitsune.app.domain.model.EngineUpdateResult
import com.kitsune.app.domain.repository.AppUpdateRepository
import com.kitsune.app.domain.repository.MediaEngine
import com.kitsune.app.domain.repository.PreferencesRepository
import com.kitsune.app.domain.util.FileSizeFormatter
import com.kitsune.app.ui.main.MainUiAction
import com.kitsune.app.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class SettingsUiState(
    val isAmoledTheme: Boolean = false,
    val isDynamicColor: Boolean = false,
    val isWifiOnly: Boolean = false,
    val isAutoCheckUpdates: Boolean = true,
    val engineVersion: String = "",
    val isCheckingEngineUpdate: Boolean = false,
    val appVersionName: String = "",
    val apkCacheSizeBytes: Long = 0L,
    val isUpdaterEnabled: Boolean = true
)

sealed interface SettingsUiAction : MainUiAction {
    data class ToggleAmoledTheme(val enabled: Boolean) : SettingsUiAction
    data class ToggleDynamicColor(val enabled: Boolean) : SettingsUiAction
    data class ToggleWifiOnly(val enabled: Boolean) : SettingsUiAction
    data class ToggleAutoCheckUpdates(val enabled: Boolean) : SettingsUiAction
    data object CheckEngineUpdate : SettingsUiAction
    data object RefreshUpdateCache : SettingsUiAction
    data object ClearUpdateCache : SettingsUiAction
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val mediaEngine: MediaEngine,
    private val appUpdateRepository: AppUpdateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            appVersionName = appUpdateRepository.currentVersionName(),
            isUpdaterEnabled = appUpdateRepository.isUpdaterEnabled
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _messages = Channel<UiText>(Channel.BUFFERED)
    val messages: Flow<UiText> = _messages.receiveAsFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.isAmoledTheme.collect { value -> _uiState.update { it.copy(isAmoledTheme = value) } }
        }
        viewModelScope.launch {
            preferencesRepository.isDynamicColor.collect { value -> _uiState.update { it.copy(isDynamicColor = value) } }
        }
        viewModelScope.launch {
            preferencesRepository.isWifiOnly.collect { value -> _uiState.update { it.copy(isWifiOnly = value) } }
        }
        viewModelScope.launch {
            preferencesRepository.isAutoCheckUpdates.collect { value ->
                _uiState.update { it.copy(isAutoCheckUpdates = value) }
            }
        }
        viewModelScope.launch {
            val version = mediaEngine.engineVersion()
            _uiState.update { it.copy(engineVersion = version) }
        }
        refreshUpdateCache()
    }

    fun onAction(action: SettingsUiAction) {
        when (action) {
            is SettingsUiAction.ToggleAmoledTheme -> persist { setAmoledTheme(action.enabled) }
            is SettingsUiAction.ToggleDynamicColor -> persist { setDynamicColor(action.enabled) }
            is SettingsUiAction.ToggleWifiOnly -> persist { setWifiOnly(action.enabled) }
            is SettingsUiAction.ToggleAutoCheckUpdates -> persist { setAutoCheckUpdates(action.enabled) }
            SettingsUiAction.CheckEngineUpdate -> checkEngineUpdate()
            SettingsUiAction.RefreshUpdateCache -> refreshUpdateCache()
            SettingsUiAction.ClearUpdateCache -> clearUpdateCache()
        }
    }

    private fun persist(block: suspend PreferencesRepository.() -> Unit) {
        viewModelScope.launch { preferencesRepository.block() }
    }

    private fun checkEngineUpdate() {
        if (_uiState.value.isCheckingEngineUpdate) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingEngineUpdate = true) }
            val result = mediaEngine.updateEngine()
            val version = mediaEngine.engineVersion()
            _uiState.update { it.copy(isCheckingEngineUpdate = false, engineVersion = version) }
            _messages.send(
                when (result) {
                    is EngineUpdateResult.Updated ->
                        UiText.StringResource(R.string.settings_engine_updated_to, listOf(result.version))
                    EngineUpdateResult.AlreadyUpToDate ->
                        UiText.StringResource(R.string.settings_engine_updated)
                    is EngineUpdateResult.Error ->
                        UiText.StringResource(R.string.settings_engine_update_failed, listOf(result.message))
                }
            )
        }
    }

    private fun refreshUpdateCache() {
        viewModelScope.launch {
            val size = appUpdateRepository.cacheSizeBytes()
            _uiState.update { it.copy(apkCacheSizeBytes = size) }
        }
    }

    private fun clearUpdateCache() {
        viewModelScope.launch {
            val freedBytes = appUpdateRepository.clearCache()
            _uiState.update { it.copy(apkCacheSizeBytes = appUpdateRepository.cacheSizeBytes()) }
            _messages.send(
                if (freedBytes > 0) {
                    UiText.StringResource(R.string.settings_clear_cache_success, listOf(FileSizeFormatter.format(freedBytes)))
                } else {
                    UiText.StringResource(R.string.settings_clear_cache_empty)
                }
            )
        }
    }
}
