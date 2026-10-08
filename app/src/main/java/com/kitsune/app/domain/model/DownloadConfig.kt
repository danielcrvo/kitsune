package com.kitsune.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
enum class VideoQuality(
    val label: String,
    val maxResolution: Int,
    val ytDlpFormatSelector: String
) {
    AUTO("Maximum (Up to 4K)", 2160, "bestvideo+bestaudio/best"),
    Q_2160P("4K (2160p)", 2160, "bestvideo[height<=2160]+bestaudio/best[height<=2160]/best"),
    Q_1440P("2K (1440p)", 1440, "bestvideo[height<=1440]+bestaudio/best[height<=1440]/best"),
    Q_1080P("Full HD (1080p)", 1080, "bestvideo[height<=1080]+bestaudio/best[height<=1080]/best"),
    Q_720P("HD (720p)", 720, "bestvideo[height<=720]+bestaudio/best[height<=720]/best"),
    Q_480P("SD (480p)", 480, "bestvideo[height<=480]+bestaudio/best[height<=480]/best"),
    AUDIO_ONLY("Audio Only (Best)", 0, "bestaudio/best");

    val ytDlpVideoOnlySelector: String
        get() = when (this) {
            AUTO, AUDIO_ONLY -> "bestvideo/best"
            else -> "bestvideo[height<=$maxResolution]/best[height<=$maxResolution]/best"
        }
}

@Immutable
enum class AudioCodec(val formatName: String, val extension: String) {
    MP3("MP3 (Universal)", "mp3"),
    ORIGINAL("Original (Best fidelity)", "m4a"),
    OPUS("Opus (High fidelity compact)", "opus")
}

@Immutable
enum class AudioQuality(
    val label: String,
    val bitrateKbps: Int,
    val ytDlpQuality: String
) {
    BEST("320 kbps (Best)", 320, "0"),
    HIGH("256 kbps (High)", 256, "2"),
    MEDIUM("192 kbps (Standard)", 192, "4"),
    LOW("128 kbps (Economy)", 128, "6")
}

@Immutable
data class DownloadConfig(
    val quality: VideoQuality = VideoQuality.AUTO,
    val audioOnly: Boolean = false,
    val audioCodec: AudioCodec = AudioCodec.MP3,
    val audioQuality: AudioQuality = AudioQuality.BEST,
    val embedSubtitles: Boolean = false,
    val muteAudio: Boolean = false
) {

    val mode: DownloadMode
        get() = when {
            audioOnly -> DownloadMode.AUDIO
            muteAudio -> DownloadMode.MUTE
            else -> DownloadMode.AUTO
        }

    fun withMode(mode: DownloadMode): DownloadConfig = when (mode) {
        DownloadMode.AUTO -> copy(audioOnly = false, muteAudio = false)
        DownloadMode.AUDIO -> copy(audioOnly = true, muteAudio = false)
        DownloadMode.MUTE -> copy(audioOnly = false, muteAudio = true)
    }
}
