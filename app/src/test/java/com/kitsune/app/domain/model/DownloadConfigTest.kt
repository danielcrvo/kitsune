package com.kitsune.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadConfigTest {

    @Test
    fun defaultConfigurationHasStandardSettings() {
        val config = DownloadConfig()

        assertEquals(VideoQuality.AUTO, config.quality)
        assertFalse(config.audioOnly)
        assertEquals(AudioCodec.MP3, config.audioCodec)
        assertEquals(AudioQuality.BEST, config.audioQuality)
        assertFalse(config.embedSubtitles)
        assertFalse(config.muteAudio)
    }

    @Test
    fun videoQualityResolutionsAndSelectorsMatchExpectedValues() {
        assertEquals(2160, VideoQuality.AUTO.maxResolution)
        assertEquals(2160, VideoQuality.Q_2160P.maxResolution)
        assertEquals(1440, VideoQuality.Q_1440P.maxResolution)
        assertEquals(1080, VideoQuality.Q_1080P.maxResolution)
        assertEquals(720, VideoQuality.Q_720P.maxResolution)
        assertEquals(480, VideoQuality.Q_480P.maxResolution)
        assertEquals(0, VideoQuality.AUDIO_ONLY.maxResolution)

        assertEquals("bestaudio/best", VideoQuality.AUDIO_ONLY.ytDlpFormatSelector)
    }

    @Test
    fun audioCodecsHaveProperExtensions() {
        assertEquals("mp3", AudioCodec.MP3.extension)
        assertEquals("m4a", AudioCodec.ORIGINAL.extension)
        assertEquals("opus", AudioCodec.OPUS.extension)
    }

    @Test
    fun audioQualityValuesMatchBitrateMappings() {
        assertEquals(320, AudioQuality.BEST.bitrateKbps)
        assertEquals("0", AudioQuality.BEST.ytDlpQuality)

        assertEquals(256, AudioQuality.HIGH.bitrateKbps)
        assertEquals("2", AudioQuality.HIGH.ytDlpQuality)

        assertEquals(192, AudioQuality.MEDIUM.bitrateKbps)
        assertEquals("4", AudioQuality.MEDIUM.ytDlpQuality)

        assertEquals(128, AudioQuality.LOW.bitrateKbps)
        assertEquals("6", AudioQuality.LOW.ytDlpQuality)
    }

    @Test
    fun customConfigurationCopiesPropertiesAccurately() {
        val original = DownloadConfig()
        val modified = original.copy(
            quality = VideoQuality.Q_720P,
            audioOnly = true,
            audioCodec = AudioCodec.OPUS,
            audioQuality = AudioQuality.HIGH,
            embedSubtitles = true,
            muteAudio = false
        )

        assertEquals(VideoQuality.Q_720P, modified.quality)
        assertTrue(modified.audioOnly)
        assertEquals(AudioCodec.OPUS, modified.audioCodec)
        assertEquals(AudioQuality.HIGH, modified.audioQuality)
        assertTrue(modified.embedSubtitles)
        assertFalse(modified.muteAudio)
    }

    @Test
    fun modeIsDerivedFromAudioFlags() {
        assertEquals(DownloadMode.AUTO, DownloadConfig().mode)
        assertEquals(DownloadMode.AUDIO, DownloadConfig(audioOnly = true).mode)
        assertEquals(DownloadMode.MUTE, DownloadConfig(muteAudio = true).mode)
    }

    @Test
    fun withModeKeepsOtherPreferences() {
        val config = DownloadConfig(
            quality = VideoQuality.Q_720P,
            audioCodec = AudioCodec.OPUS,
            embedSubtitles = true
        )

        val audio = config.withMode(DownloadMode.AUDIO)
        assertTrue(audio.audioOnly)
        assertFalse(audio.muteAudio)
        assertEquals(AudioCodec.OPUS, audio.audioCodec)
        assertEquals(VideoQuality.Q_720P, audio.quality)

        val mute = audio.withMode(DownloadMode.MUTE)
        assertFalse(mute.audioOnly)
        assertTrue(mute.muteAudio)

        val auto = mute.withMode(DownloadMode.AUTO)
        assertFalse(auto.audioOnly)
        assertFalse(auto.muteAudio)
        assertTrue(auto.embedSubtitles)
    }

    @Test
    fun videoOnlySelectorsNeverRequestAudioStreams() {
        VideoQuality.entries.forEach { quality ->
            assertFalse(quality.ytDlpVideoOnlySelector.contains("bestaudio"))
        }
        assertEquals(
            "bestvideo[height<=720]/best[height<=720]/best",
            VideoQuality.Q_720P.ytDlpVideoOnlySelector
        )
    }
}
