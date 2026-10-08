package com.kitsune.app.core.model

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

    fun encode(): String = listOf(
        KEY_QUALITY to quality.name,
        KEY_AUDIO_ONLY to audioOnly.toString(),
        KEY_AUDIO_CODEC to audioCodec.name,
        KEY_AUDIO_QUALITY to audioQuality.name,
        KEY_EMBED_SUBTITLES to embedSubtitles.toString(),
        KEY_MUTE_AUDIO to muteAudio.toString()
    ).joinToString(ENTRY_SEPARATOR) { (key, value) -> "$key$VALUE_SEPARATOR$value" }

    companion object {
        private const val ENTRY_SEPARATOR = ";"
        private const val VALUE_SEPARATOR = "="
        private const val KEY_QUALITY = "quality"
        private const val KEY_AUDIO_ONLY = "audioOnly"
        private const val KEY_AUDIO_CODEC = "audioCodec"
        private const val KEY_AUDIO_QUALITY = "audioQuality"
        private const val KEY_EMBED_SUBTITLES = "embedSubtitles"
        private const val KEY_MUTE_AUDIO = "muteAudio"

        fun decode(raw: String?): DownloadConfig {
            if (raw.isNullOrBlank()) return DownloadConfig()
            val values = raw.split(ENTRY_SEPARATOR)
                .mapNotNull { entry ->
                    val key = entry.substringBefore(VALUE_SEPARATOR, "")
                    if (key.isEmpty()) null else key to entry.substringAfter(VALUE_SEPARATOR)
                }
                .toMap()
            val defaults = DownloadConfig()
            return DownloadConfig(
                quality = values[KEY_QUALITY]
                    ?.let { runCatching { VideoQuality.valueOf(it) }.getOrNull() } ?: defaults.quality,
                audioOnly = values[KEY_AUDIO_ONLY]?.toBooleanStrictOrNull() ?: defaults.audioOnly,
                audioCodec = values[KEY_AUDIO_CODEC]
                    ?.let { runCatching { AudioCodec.valueOf(it) }.getOrNull() } ?: defaults.audioCodec,
                audioQuality = values[KEY_AUDIO_QUALITY]
                    ?.let { runCatching { AudioQuality.valueOf(it) }.getOrNull() } ?: defaults.audioQuality,
                embedSubtitles = values[KEY_EMBED_SUBTITLES]?.toBooleanStrictOrNull() ?: defaults.embedSubtitles,
                muteAudio = values[KEY_MUTE_AUDIO]?.toBooleanStrictOrNull() ?: defaults.muteAudio
            )
        }
    }
}
