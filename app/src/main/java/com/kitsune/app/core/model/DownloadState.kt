package com.kitsune.app.core.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.kitsune.app.R

@Immutable
enum class DownloadStage(@StringRes val labelRes: Int) {
    INITIALIZING(R.string.stage_initializing),
    VIDEO_STREAM(R.string.stage_video_stream),
    AUDIO_STREAM(R.string.stage_audio_stream),
    FFMPEG_MUXING(R.string.stage_ffmpeg_muxing),
    FINALIZING(R.string.stage_finalizing)
}

@Immutable
sealed interface DownloadState {


    @Immutable
    data object Idle : DownloadState

    @Immutable
    data object WaitingForWifi : DownloadState


    @Immutable
    data class FetchingInfo(val url: String) : DownloadState


    @Immutable
    data class Downloading(
        val progress: Float = 0f,
        val speed: String = "",
        val eta: String = "",
        val stage: DownloadStage = DownloadStage.INITIALIZING,
        val downloadedBytes: Long = 0L,
        val totalBytes: Long = 0L
    ) : DownloadState


    @Immutable
    data class Muxing(val message: String = "Processando no FFmpeg...") : DownloadState


    @Immutable
    data class Completed(
        val title: String,
        val outputPath: String,
        val fileSizeFormatted: String
    ) : DownloadState


    @Immutable
    data class Error(val message: String) : DownloadState
}

@Immutable
data class MediaInfo(
    val id: String,
    val title: String,
    val uploader: String,
    val durationSeconds: Long,
    val thumbnailUrl: String?,
    val platform: PlatformType,
    val originalUrl: String
)
