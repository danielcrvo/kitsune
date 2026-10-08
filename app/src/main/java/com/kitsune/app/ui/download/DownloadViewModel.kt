package com.kitsune.app.ui.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.model.DownloadMode
import com.kitsune.app.domain.model.DownloadRequest
import com.kitsune.app.domain.repository.DownloadQueueRepository
import com.kitsune.app.domain.repository.MediaEngine
import com.kitsune.app.domain.repository.PreferencesRepository
import com.kitsune.app.domain.usecase.AnalyzeLinkUseCase
import com.kitsune.app.domain.usecase.CancelDownloadsUseCase
import com.kitsune.app.domain.usecase.EnqueueDownloadsUseCase
import com.kitsune.app.domain.usecase.LoadDownloadConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val analyzeLink: AnalyzeLinkUseCase,
    private val loadDownloadConfig: LoadDownloadConfigUseCase,
    private val enqueueDownloads: EnqueueDownloadsUseCase,
    private val cancelDownloads: CancelDownloadsUseCase,
    private val mediaEngine: MediaEngine,
    private val preferencesRepository: PreferencesRepository,
    private val queueRepository: DownloadQueueRepository
) : ViewModel() {

    companion object {
        const val METADATA_DEBOUNCE_MS = 450L
    }

    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()

    private var metadataJob: Job? = null

    init {
        viewModelScope.launch {
            queueRepository.currentState.collect { state ->
                _uiState.update { it.copy(downloadState = state) }
            }
        }
        viewModelScope.launch {
            queueRepository.queue.collect { queue ->
                _uiState.update { it.copy(downloadQueue = queue) }
            }
        }
        viewModelScope.launch {
            val config = loadDownloadConfig()
            _uiState.update { it.copy(downloadConfig = config) }
        }
    }

    fun onAction(action: DownloadUiAction) {
        when (action) {
            is DownloadUiAction.ChangeUrl -> onUrlChanged(action.url)
            is DownloadUiAction.ClipboardTextAvailable -> onClipboardText(action.text)
            DownloadUiAction.StartDownload -> startDownload()
            DownloadUiAction.CancelDownload -> cancelDownloads.cancelAll()
            is DownloadUiAction.CancelQueueTask -> cancelDownloads.cancelTask(action.taskId)
            DownloadUiAction.DismissError -> queueRepository.resetCurrentState()
            is DownloadUiAction.SetDownloadMode -> setDownloadMode(action.mode)
            is DownloadUiAction.ChangeConfig -> changeConfig(action.config)
            DownloadUiAction.OpenPlaylistDialog -> openPlaylistDialog()
            DownloadUiAction.ClosePlaylistDialog -> _uiState.update { it.copy(isPlaylistDialogOpen = false) }
            is DownloadUiAction.TogglePlaylistItem -> togglePlaylistItem(action.itemId)
            DownloadUiAction.SelectAllPlaylistItems -> _uiState.update { state ->
                state.copy(selectedPlaylistItems = state.playlistInfo?.items?.map { it.id }?.toSet().orEmpty())
            }
            DownloadUiAction.DeselectAllPlaylistItems -> _uiState.update { it.copy(selectedPlaylistItems = emptySet()) }
            DownloadUiAction.DownloadSelectedPlaylistItems -> downloadSelectedPlaylistItems()
        }
    }

    private fun onClipboardText(rawText: String?) {
        val clipText = rawText?.trim()
        if (clipText.isNullOrBlank() || !analyzeLink(clipText).isValid) return

        val currentUrl = _uiState.value.url.trim()
        if (currentUrl.isBlank()) {
            onUrlChanged(clipText)
        } else if (currentUrl != clipText) {
            _uiState.update { it.copy(detectedClipboardUrl = clipText) }
        }
    }

    private fun onUrlChanged(newUrl: String) {
        metadataJob?.cancel()
        val link = analyzeLink(newUrl)
        val urlChanged = newUrl.trim() != _uiState.value.url.trim()

        _uiState.update {
            val keepMetadata = !urlChanged && newUrl.isNotBlank()
            it.copy(
                url = newUrl,
                detectedPlatform = link.platform,
                isUrlValid = link.isValid,
                mediaInfo = if (keepMetadata) it.mediaInfo else null,
                playlistInfo = if (keepMetadata) it.playlistInfo else null,
                isLoadingMetadata = link.isValid && !link.isPlaylist && (urlChanged || it.mediaInfo == null),
                isLoadingPlaylist = link.isValid && link.isPlaylist && (urlChanged || it.playlistInfo == null),
                detectedClipboardUrl = null
            )
        }

        if (!_uiState.value.isResolvingLink) return

        metadataJob = viewModelScope.launch {
            delay(METADATA_DEBOUNCE_MS)
            if (link.isPlaylist) {
                mediaEngine.fetchPlaylistInfo(newUrl).fold(
                    onSuccess = { playlist ->
                        _uiState.update {
                            it.copy(
                                playlistInfo = playlist,
                                selectedPlaylistItems = playlist.items.map { item -> item.id }.toSet(),
                                isPlaylistDialogOpen = true,
                                isLoadingPlaylist = false
                            )
                        }
                    },
                    onFailure = { _uiState.update { it.copy(isLoadingPlaylist = false) } }
                )
            } else {
                mediaEngine.fetchMediaInfo(newUrl).fold(
                    onSuccess = { info -> _uiState.update { it.copy(mediaInfo = info, isLoadingMetadata = false) } },
                    onFailure = { _uiState.update { it.copy(mediaInfo = null, isLoadingMetadata = false) } }
                )
            }
        }
    }

    private fun startDownload() {
        val state = _uiState.value
        if (!state.isUrlValid) return
        enqueueDownloads(
            requests = listOf(DownloadRequest(url = state.url, title = state.mediaInfo?.title.orEmpty())),
            config = state.downloadConfig
        )
    }

    private fun setDownloadMode(mode: DownloadMode) {
        _uiState.update { it.copy(downloadConfig = it.downloadConfig.withMode(mode)) }
        viewModelScope.launch { preferencesRepository.saveDownloadMode(mode) }
    }

    private fun changeConfig(config: DownloadConfig) {
        _uiState.update { it.copy(downloadConfig = config) }
        viewModelScope.launch { preferencesRepository.saveDownloadConfig(config) }
    }

    private fun openPlaylistDialog() {
        if (_uiState.value.playlistInfo != null) {
            _uiState.update { it.copy(isPlaylistDialogOpen = true) }
        }
    }

    private fun togglePlaylistItem(itemId: String) {
        _uiState.update { state ->
            val current = state.selectedPlaylistItems
            state.copy(selectedPlaylistItems = if (itemId in current) current - itemId else current + itemId)
        }
    }

    private fun downloadSelectedPlaylistItems() {
        val state = _uiState.value
        val playlist = state.playlistInfo ?: return
        val selected = playlist.items.filter { it.id in state.selectedPlaylistItems }
        if (selected.isEmpty()) return

        enqueueDownloads(
            requests = selected.map { DownloadRequest(url = it.url, title = it.title) },
            config = state.downloadConfig
        )

        _uiState.update {
            it.copy(
                isPlaylistDialogOpen = false,
                url = "",
                isUrlValid = false,
                playlistInfo = null,
                selectedPlaylistItems = emptySet()
            )
        }
    }
}
