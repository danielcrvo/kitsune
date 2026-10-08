package com.kitsune.app.ui.download

import androidx.compose.runtime.Immutable
import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.model.DownloadMode
import com.kitsune.app.domain.model.DownloadState
import com.kitsune.app.domain.model.DownloadTask
import com.kitsune.app.domain.model.MediaInfo
import com.kitsune.app.domain.model.PlatformType
import com.kitsune.app.domain.model.PlaylistInfo

@Immutable
data class DownloadUiState(
    val url: String = "",
    val detectedPlatform: PlatformType = PlatformType.UNKNOWN,
    val isUrlValid: Boolean = false,
    val mediaInfo: MediaInfo? = null,
    val isLoadingMetadata: Boolean = false,
    val detectedClipboardUrl: String? = null,
    val downloadConfig: DownloadConfig = DownloadConfig(),
    val downloadState: DownloadState = DownloadState.Idle,
    val downloadQueue: List<DownloadTask> = emptyList(),
    val playlistInfo: PlaylistInfo? = null,
    val isPlaylistDialogOpen: Boolean = false,
    val selectedPlaylistItems: Set<String> = emptySet(),
    val isLoadingPlaylist: Boolean = false
) {
    val downloadMode: DownloadMode
        get() = downloadConfig.mode

    val isResolvingLink: Boolean
        get() = isLoadingMetadata || isLoadingPlaylist

    val hasActiveOrQueuedDownloads: Boolean
        get() = downloadState !is DownloadState.Idle || downloadQueue.any { it.state is DownloadState.Idle }
}
