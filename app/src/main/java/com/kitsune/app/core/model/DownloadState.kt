package com.kitsune.app.core.model

import androidx.compose.runtime.Immutable

@Immutable
enum class DownloadStage(val description: String) {
    INITIALIZING("Preparando download..."),
    VIDEO_STREAM("Baixando stream de vídeo..."),
    AUDIO_STREAM("Baixando áudio..."),
    FFMPEG_MUXING("Unindo vídeo e áudio com FFmpeg..."),
    FINALIZING("Salvando na Galeria do Android...")
}

@Immutable
sealed interface DownloadState {


    @Immutable
    data object Idle : DownloadState


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
