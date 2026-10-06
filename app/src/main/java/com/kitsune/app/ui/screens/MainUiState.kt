package com.kitsune.app.ui.screens

import androidx.compose.runtime.Immutable
import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.DownloadState
import com.kitsune.app.core.model.DownloadedMediaFile
import com.kitsune.app.core.model.MediaInfo
import com.kitsune.app.core.model.PlatformType

import com.kitsune.app.core.model.AppUpdateInfo
import com.kitsune.app.core.model.AppUpdateState
import com.kitsune.app.core.model.DownloadTask
import com.kitsune.app.core.model.PlaylistInfo

enum class DownloadMode {
    AUTO,
    AUDIO,
    MUTE
}

@Immutable
data class MainUiState(
    val url: String = "",
    val detectedPlatform: PlatformType = PlatformType.UNKNOWN,
    val isUrlValid: Boolean = false,
    val mediaInfo: MediaInfo? = null,
    val isLoadingMetadata: Boolean = false,
    val detectedClipboardUrl: String? = null,
    val downloadConfig: DownloadConfig = DownloadConfig(),
    val downloadState: DownloadState = DownloadState.Idle,
    val isSettingsSheetOpen: Boolean = false,
    val isSupportedServicesOpen: Boolean = false,
    val isTermsOpen: Boolean = false,
    val isHistoryOpen: Boolean = false,
    val engineVersion: String = "",
    val isCheckingUpdate: Boolean = false,
    val toastMessage: String? = null,
    val downloadedFiles: List<DownloadedMediaFile> = emptyList(),
    val isLoadingDownloadedFiles: Boolean = false,
    val renamingFile: DownloadedMediaFile? = null,
    val deletingFile: DownloadedMediaFile? = null,
    val playingFile: DownloadedMediaFile? = null,
    val isAmoledTheme: Boolean = false,
    val isDynamicColor: Boolean = false,
    val isWifiOnly: Boolean = false,
    val downloadQueue: List<DownloadTask> = emptyList(),
    val playlistInfo: PlaylistInfo? = null,
    val isPlaylistDialogOpen: Boolean = false,
    val selectedPlaylistItems: Set<String> = emptySet(),
    val isLoadingPlaylist: Boolean = false,
    val appVersionName: String = "",
    val appUpdateInfo: AppUpdateInfo? = null,
    val appUpdateState: AppUpdateState = AppUpdateState.Idle,
    val isAppUpdateDialogOpen: Boolean = false,
    val isCheckingAppUpdate: Boolean = false
) {
    val downloadMode: DownloadMode
        get() = when {
            downloadConfig.audioOnly -> DownloadMode.AUDIO
            downloadConfig.muteAudio -> DownloadMode.MUTE
            else -> DownloadMode.AUTO
        }
}
