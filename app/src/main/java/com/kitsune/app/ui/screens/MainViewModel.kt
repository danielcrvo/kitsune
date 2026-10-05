package com.kitsune.app.ui.screens

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kitsune.app.core.engine.EngineUpdateManager
import com.kitsune.app.core.engine.UrlDetector
import com.kitsune.app.core.engine.YtDlpEngine
import com.kitsune.app.core.model.AudioCodec
import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.DownloadState
import com.kitsune.app.core.model.DownloadedMediaFile
import com.kitsune.app.core.service.DownloadForegroundService
import com.kitsune.app.core.storage.DownloadedFilesRepository
import com.kitsune.app.core.storage.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository(application)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var metadataJob: Job? = null

    init {
        viewModelScope.launch {
            DownloadForegroundService.currentDownloadState.collect { state ->
                _uiState.update { it.copy(downloadState = state) }
                if (state is DownloadState.Completed) {
                    loadDownloadedFiles()
                }
            }
        }

        viewModelScope.launch {
            val savedConfig = preferencesRepository.userConfigFlow.firstOrNull()
            val savedMode = preferencesRepository.downloadModeFlow.firstOrNull() ?: DownloadMode.AUTO

            if (savedConfig != null) {
                val configWithMode = when (savedMode) {
                    DownloadMode.AUTO -> savedConfig.copy(audioOnly = false, muteAudio = false)
                    DownloadMode.AUDIO -> savedConfig.copy(audioOnly = true, muteAudio = false, audioCodec = AudioCodec.MP3)
                    DownloadMode.MUTE -> savedConfig.copy(audioOnly = false, muteAudio = true)
                }
                _uiState.update { it.copy(downloadConfig = configWithMode) }
            }
        }

        viewModelScope.launch {
            preferencesRepository.isAmoledThemeFlow.collect { amoled ->
                _uiState.update { it.copy(isAmoledTheme = amoled) }
            }
        }

        loadDownloadedFiles()
    }

    fun initEngineVersion(context: Context) {
        viewModelScope.launch {
            try {
                val version = EngineUpdateManager.getEngineVersion(context)
                _uiState.update { it.copy(engineVersion = version) }
            } catch (t: Throwable) {
                _uiState.update { it.copy(engineVersion = "N/A") }
            }
        }
    }

    fun checkClipboardForMediaUrl(context: Context) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()

            if (!clipText.isNullOrBlank() && UrlDetector.isValidUrl(clipText)) {
                val currentUrl = _uiState.value.url.trim()
                if (currentUrl.isBlank()) {
                    onUrlChanged(clipText)
                } else if (currentUrl != clipText) {
                    _uiState.update { it.copy(detectedClipboardUrl = clipText) }
                }
            }
        } catch (_: Throwable) {}
    }

    fun onUrlChanged(newUrl: String) {
        metadataJob?.cancel()
        val detected = UrlDetector.detect(newUrl)
        val isValid = UrlDetector.isValidUrl(newUrl)

        _uiState.update {
            it.copy(
                url = newUrl,
                detectedPlatform = detected,
                isUrlValid = isValid,
                mediaInfo = if (newUrl.isBlank()) null else it.mediaInfo,
                isLoadingMetadata = isValid && it.mediaInfo == null,
                detectedClipboardUrl = null
            )
        }

        if (isValid) {
            metadataJob = viewModelScope.launch {
                try {
                    _uiState.update { it.copy(isLoadingMetadata = true) }
                    val result = YtDlpEngine.fetchMediaInfo(getApplication(), newUrl)
                    result.fold(
                        onSuccess = { info ->
                            _uiState.update {
                                it.copy(
                                    mediaInfo = info,
                                    isLoadingMetadata = false
                                )
                            }
                        },
                        onFailure = {
                            _uiState.update { it.copy(isLoadingMetadata = false) }
                        }
                    )
                } catch (_: Throwable) {
                    _uiState.update { it.copy(isLoadingMetadata = false) }
                }
            }
        }
    }

    fun onConfigChanged(newConfig: DownloadConfig) {
        _uiState.update { it.copy(downloadConfig = newConfig) }
        viewModelScope.launch {
            preferencesRepository.saveDownloadConfig(newConfig)
        }
    }

    fun setDownloadMode(mode: DownloadMode) {
        _uiState.update { state ->
            val newConfig = when (mode) {
                DownloadMode.AUTO -> state.downloadConfig.copy(audioOnly = false, muteAudio = false)
                DownloadMode.AUDIO -> state.downloadConfig.copy(audioOnly = true, muteAudio = false, audioCodec = AudioCodec.MP3)
                DownloadMode.MUTE -> state.downloadConfig.copy(audioOnly = false, muteAudio = true)
            }
            state.copy(downloadConfig = newConfig)
        }
        viewModelScope.launch {
            preferencesRepository.saveDownloadMode(mode)
        }
    }

    fun toggleSettingsSheet(open: Boolean) {
        _uiState.update { it.copy(isSettingsSheetOpen = open) }
    }

    fun toggleSupportedServices(open: Boolean) {
        _uiState.update { it.copy(isSupportedServicesOpen = open) }
    }

    fun toggleTerms(open: Boolean) {
        _uiState.update { it.copy(isTermsOpen = open) }
    }

    fun toggleHistory(open: Boolean) {
        _uiState.update { it.copy(isHistoryOpen = open) }
        if (open) {
            loadDownloadedFiles()
        }
    }

    fun loadDownloadedFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDownloadedFiles = true) }
            val files = DownloadedFilesRepository.getDownloadedFiles(getApplication())
            _uiState.update {
                it.copy(
                    downloadedFiles = files,
                    isLoadingDownloadedFiles = false
                )
            }
        }
    }

    fun playDownloadedFile(item: DownloadedMediaFile) {
        _uiState.update { it.copy(playingFile = item) }
    }

    fun closeMediaPlayer() {
        _uiState.update { it.copy(playingFile = null) }
    }

    fun playFileExternal(context: Context, item: DownloadedMediaFile) {
        DownloadedFilesRepository.playFile(context, item)
    }

    fun setRenamingFile(item: DownloadedMediaFile?) {
        _uiState.update { it.copy(renamingFile = item) }
    }

    fun setDeletingFile(item: DownloadedMediaFile?) {
        _uiState.update { it.copy(deletingFile = item) }
    }

    fun confirmDeleteFile(item: DownloadedMediaFile) {
        viewModelScope.launch {
            val success = DownloadedFilesRepository.deleteFile(getApplication(), item)
            _uiState.update { state ->
                state.copy(
                    deletingFile = null,
                    downloadedFiles = state.downloadedFiles.filterNot { it.id == item.id || it.uri == item.uri },
                    toastMessage = if (success) "Arquivo excluído com sucesso." else "Não foi possível excluir o arquivo."
                )
            }
        }
    }

    fun confirmRenameFile(item: DownloadedMediaFile, newNameWithoutExt: String) {
        viewModelScope.launch {
            val result = DownloadedFilesRepository.renameFile(getApplication(), item, newNameWithoutExt)
            result.fold(
                onSuccess = { updatedItem ->
                    _uiState.update { state ->
                        state.copy(
                            renamingFile = null,
                            downloadedFiles = state.downloadedFiles.map { if (it.id == item.id) updatedItem else it },
                            toastMessage = "Arquivo renomeado com sucesso!"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            renamingFile = null,
                            toastMessage = "Erro ao renomear: ${err.localizedMessage ?: "Nome inválido"}"
                        )
                    }
                }
            )
        }
    }

    fun startDownload(context: Context) {
        val currentState = _uiState.value
        if (!currentState.isUrlValid) return

        DownloadForegroundService.startDownload(
            context = context,
            url = currentState.url,
            config = currentState.downloadConfig
        )
    }

    fun cancelDownload(context: Context) {
        DownloadForegroundService.cancelDownload(context)
    }

    fun dismissError() {
        _uiState.update { it.copy(downloadState = DownloadState.Idle) }
    }

    fun checkEngineUpdate(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUpdate = true) }
            val status = EngineUpdateManager.checkForUpdates(context)
            val newVersion = EngineUpdateManager.getEngineVersion(context)
            _uiState.update {
                it.copy(
                    isCheckingUpdate = false,
                    engineVersion = newVersion,
                    toastMessage = when (status) {
                        is EngineUpdateManager.UpdateStatus.Updated -> "Engine yt-dlp atualizada para v${status.version}!"
                        is EngineUpdateManager.UpdateStatus.AlreadyUpToDate -> "A engine já está na versão mais recente."
                        is EngineUpdateManager.UpdateStatus.Error -> "Erro ao atualizar engine: ${status.error}"
                        else -> null
                    }
                )
            }
        }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun toggleAmoledTheme(enabled: Boolean) {
        _uiState.update { it.copy(isAmoledTheme = enabled) }
        viewModelScope.launch {
            preferencesRepository.saveAmoledTheme(enabled)
        }
    }
}
