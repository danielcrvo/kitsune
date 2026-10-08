package com.kitsune.app.ui.history

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitsune.app.R
import com.kitsune.app.domain.model.DownloadState
import com.kitsune.app.domain.model.DownloadedMediaFile
import com.kitsune.app.domain.repository.DownloadQueueRepository
import com.kitsune.app.domain.repository.MediaLibraryRepository
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
data class HistoryUiState(
    val files: List<DownloadedMediaFile> = emptyList(),
    val isLoading: Boolean = false,
    val playingFile: DownloadedMediaFile? = null
)

sealed interface HistoryUiAction : MainUiAction {
    data object Refresh : HistoryUiAction
    data class PlayFile(val file: DownloadedMediaFile) : HistoryUiAction
    data object CloseMediaPlayer : HistoryUiAction
    data class PlayExternal(val file: DownloadedMediaFile) : HistoryUiAction
    data class DeleteFile(val file: DownloadedMediaFile) : HistoryUiAction
    data class RenameFile(val file: DownloadedMediaFile, val newName: String) : HistoryUiAction
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val mediaLibraryRepository: MediaLibraryRepository,
    queueRepository: DownloadQueueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _messages = Channel<UiText>(Channel.BUFFERED)
    val messages: Flow<UiText> = _messages.receiveAsFlow()

    init {
        viewModelScope.launch {
            queueRepository.currentState.collect { state ->
                if (state is DownloadState.Completed) refresh()
            }
        }
        refresh()
    }

    fun onAction(action: HistoryUiAction) {
        when (action) {
            HistoryUiAction.Refresh -> refresh()
            is HistoryUiAction.PlayFile -> _uiState.update { it.copy(playingFile = action.file) }
            HistoryUiAction.CloseMediaPlayer -> _uiState.update { it.copy(playingFile = null) }
            is HistoryUiAction.PlayExternal -> mediaLibraryRepository.openExternally(action.file)
            is HistoryUiAction.DeleteFile -> delete(action.file)
            is HistoryUiAction.RenameFile -> rename(action.file, action.newName)
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val files = mediaLibraryRepository.getDownloadedFiles()
            _uiState.update { it.copy(files = files, isLoading = false) }
        }
    }

    private fun delete(item: DownloadedMediaFile) {
        viewModelScope.launch {
            val deleted = mediaLibraryRepository.delete(item)
            _uiState.update { state ->
                state.copy(files = state.files.filterNot { it.id == item.id || it.uri == item.uri })
            }
            _messages.send(
                UiText.StringResource(
                    if (deleted) R.string.history_file_deleted else R.string.history_file_delete_failed
                )
            )
        }
    }

    private fun rename(item: DownloadedMediaFile, newName: String) {
        viewModelScope.launch {
            mediaLibraryRepository.rename(item, newName).fold(
                onSuccess = { updated ->
                    _uiState.update { state ->
                        state.copy(files = state.files.map { if (it.id == item.id) updated else it })
                    }
                    _messages.send(UiText.StringResource(R.string.history_file_renamed))
                },
                onFailure = { error ->
                    _messages.send(
                        UiText.StringResource(R.string.history_file_rename_failed, listOf(error.localizedMessage.orEmpty()))
                    )
                }
            )
        }
    }
}
