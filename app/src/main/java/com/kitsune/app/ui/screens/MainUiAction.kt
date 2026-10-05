package com.kitsune.app.ui.screens

import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.DownloadedMediaFile

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
    data object CheckEngineUpdate : MainUiAction
    data class PlayFile(val file: DownloadedMediaFile) : MainUiAction
    data object CloseMediaPlayer : MainUiAction
    data class PlayExternal(val file: DownloadedMediaFile) : MainUiAction
    data class DeleteFile(val file: DownloadedMediaFile) : MainUiAction
    data class RenameFile(val file: DownloadedMediaFile, val newName: String) : MainUiAction
}
