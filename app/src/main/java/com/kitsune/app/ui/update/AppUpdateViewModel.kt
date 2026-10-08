package com.kitsune.app.ui.update

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitsune.app.R
import com.kitsune.app.domain.model.AppUpdateInfo
import com.kitsune.app.domain.model.AppUpdateState
import com.kitsune.app.domain.repository.AppUpdateRepository
import com.kitsune.app.domain.usecase.CheckForAppUpdateUseCase
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
import java.io.File
import javax.inject.Inject

@Immutable
data class AppUpdateUiState(
    val updateState: AppUpdateState = AppUpdateState.Idle,
    val availableUpdate: AppUpdateInfo? = null,
    val isDialogOpen: Boolean = false,
    val isChecking: Boolean = false
)

sealed interface AppUpdateUiAction : MainUiAction {
    data object CheckForUpdate : AppUpdateUiAction
    data object DownloadUpdate : AppUpdateUiAction
    data object InstallUpdate : AppUpdateUiAction
    data object DismissDialog : AppUpdateUiAction
}

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val checkForAppUpdate: CheckForAppUpdateUseCase,
    private val appUpdateRepository: AppUpdateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUpdateUiState())
    val uiState: StateFlow<AppUpdateUiState> = _uiState.asStateFlow()

    private val _messages = Channel<UiText>(Channel.BUFFERED)
    val messages: Flow<UiText> = _messages.receiveAsFlow()

    init {
        viewModelScope.launch {
            checkForAppUpdate(isManualCheck = false).onSuccess { info ->
                if (info != null) showAvailableUpdate(info)
            }
        }
    }

    fun onAction(action: AppUpdateUiAction) {
        when (action) {
            AppUpdateUiAction.CheckForUpdate -> checkManually()
            AppUpdateUiAction.DownloadUpdate -> downloadUpdate()
            AppUpdateUiAction.InstallUpdate -> installUpdate()
            AppUpdateUiAction.DismissDialog -> _uiState.update { it.copy(isDialogOpen = false) }
        }
    }

    private fun showAvailableUpdate(info: AppUpdateInfo) {
        _uiState.update {
            it.copy(
                availableUpdate = info,
                updateState = AppUpdateState.UpdateAvailable(info),
                isDialogOpen = true
            )
        }
    }

    private fun checkManually() {
        if (!appUpdateRepository.isUpdaterEnabled || _uiState.value.isChecking) return
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true) }
            val result = checkForAppUpdate(isManualCheck = true)
            _uiState.update { it.copy(isChecking = false) }
            result.fold(
                onSuccess = { info ->
                    if (info != null) {
                        showAvailableUpdate(info)
                    } else {
                        _messages.send(UiText.StringResource(R.string.settings_app_up_to_date))
                    }
                },
                onFailure = { error ->
                    _messages.send(
                        UiText.StringResource(R.string.update_download_failed, listOf(error.localizedMessage.orEmpty()))
                    )
                }
            )
        }
    }

    private fun downloadUpdate() {
        val info = _uiState.value.availableUpdate ?: return
        if (!appUpdateRepository.isUpdaterEnabled || _uiState.value.updateState is AppUpdateState.Downloading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(updateState = AppUpdateState.Downloading(info, 0, 0L, info.fileSizeBytes))
            }
            val result = appUpdateRepository.downloadUpdate(info) { percent, read, total ->
                _uiState.update { it.copy(updateState = AppUpdateState.Downloading(info, percent, read, total)) }
            }
            _uiState.update {
                it.copy(
                    updateState = result.fold(
                        onSuccess = { apk -> AppUpdateState.ReadyToInstall(info, apk.absolutePath) },
                        onFailure = { error -> AppUpdateState.Error(error.localizedMessage ?: "Download failed") }
                    )
                )
            }
        }
    }

    private fun installUpdate() {
        val state = _uiState.value.updateState as? AppUpdateState.ReadyToInstall ?: return
        if (!appUpdateRepository.install(File(state.apkPath))) {
            viewModelScope.launch {
                _messages.send(UiText.StringResource(R.string.update_permission_required))
            }
        }
    }
}
