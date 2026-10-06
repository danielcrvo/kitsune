package com.kitsune.app.ui.screens

import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.DownloadedMediaFile
import com.kitsune.app.core.model.PlaylistInfo

sealed interface MainUiAction {
    data class ChangeUrl(val url: String) : MainUiAction
    data object StartDownload : MainUiAction
    data object CancelDownload : MainUiAction
    data object DismissError : MainUiAction
    data class SetDownloadMode(val mode: DownloadMode) : MainUiAction
    data class ChangeConfig(val config: DownloadConfig) : MainUiAction
    data class ToggleSettings(val open: Boolean) : MainUiAction
    data class ToggleHistory(val open: Boolean) : MainUiAction
    data class ToggleSupportedServices(val open: Boolean) : MainUiAction
    data class ToggleTerms(val open: Boolean) : MainUiAction
    data class ToggleAmoledTheme(val enabled: Boolean) : MainUiAction
    data class ToggleDynamicColor(val enabled: Boolean) : MainUiAction
    data class ToggleWifiOnly(val enabled: Boolean) : MainUiAction
    data class CancelQueueTask(val taskId: String) : MainUiAction
    data object CheckEngineUpdate : MainUiAction
    data class PlayFile(val file: DownloadedMediaFile) : MainUiAction
    data object CloseMediaPlayer : MainUiAction
    data class PlayExternal(val file: DownloadedMediaFile) : MainUiAction
    data class DeleteFile(val file: DownloadedMediaFile) : MainUiAction
    data class RenameFile(val file: DownloadedMediaFile, val newName: String) : MainUiAction
    data class OpenPlaylistDialog(val info: PlaylistInfo) : MainUiAction
    data object ClosePlaylistDialog : MainUiAction
    data class TogglePlaylistItem(val itemId: String) : MainUiAction
    data object SelectAllPlaylistItems : MainUiAction
    data object DeselectAllPlaylistItems : MainUiAction
    data object DownloadSelectedPlaylistItems : MainUiAction
    data object CheckAppUpdate : MainUiAction
    data object DownloadAppUpdate : MainUiAction
    data object InstallAppUpdate : MainUiAction
    data object DismissAppUpdateDialog : MainUiAction
}
