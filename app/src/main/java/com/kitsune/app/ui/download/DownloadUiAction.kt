package com.kitsune.app.ui.download

import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.model.DownloadMode
import com.kitsune.app.ui.main.MainUiAction

sealed interface DownloadUiAction : MainUiAction {
    data class ChangeUrl(val url: String) : DownloadUiAction
    data class ClipboardTextAvailable(val text: String?) : DownloadUiAction
    data object StartDownload : DownloadUiAction
    data object CancelDownload : DownloadUiAction
    data class CancelQueueTask(val taskId: String) : DownloadUiAction
    data object DismissError : DownloadUiAction
    data class SetDownloadMode(val mode: DownloadMode) : DownloadUiAction
    data class ChangeConfig(val config: DownloadConfig) : DownloadUiAction
    data object OpenPlaylistDialog : DownloadUiAction
    data object ClosePlaylistDialog : DownloadUiAction
    data class TogglePlaylistItem(val itemId: String) : DownloadUiAction
    data object SelectAllPlaylistItems : DownloadUiAction
    data object DeselectAllPlaylistItems : DownloadUiAction
    data object DownloadSelectedPlaylistItems : DownloadUiAction
}
