package com.kitsune.app.ui.screens

import com.kitsune.app.core.model.AudioCodec
import com.kitsune.app.core.model.AudioQuality
import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.DownloadState
import com.kitsune.app.core.model.PlatformType
import com.kitsune.app.core.model.VideoQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainUiStateTest {

    @Test
    fun defaultStateHasExpectedInitialValues() {
        val state = MainUiState()

        assertEquals("", state.url)
        assertEquals(PlatformType.UNKNOWN, state.detectedPlatform)
        assertFalse(state.isUrlValid)
        assertNull(state.mediaInfo)
        assertFalse(state.isLoadingMetadata)
        assertNull(state.detectedClipboardUrl)
        assertEquals(DownloadState.Idle, state.downloadState)
        assertEquals(DownloadMode.AUTO, state.downloadMode)
        assertFalse(state.isSettingsSheetOpen)
        assertFalse(state.isSupportedServicesOpen)
        assertFalse(state.isTermsOpen)
        assertFalse(state.isHistoryOpen)
        assertFalse(state.isAmoledTheme)
        assertTrue(state.downloadedFiles.isEmpty())
        assertNull(state.toastMessage)
        assertNull(state.playingFile)
    }

    @Test
    fun downloadModeResolvesCorrectlyFromConfig() {
        val autoState = MainUiState(downloadConfig = DownloadConfig(audioOnly = false, muteAudio = false))
        assertEquals(DownloadMode.AUTO, autoState.downloadMode)

        val audioState = MainUiState(downloadConfig = DownloadConfig(audioOnly = true, muteAudio = false))
        assertEquals(DownloadMode.AUDIO, audioState.downloadMode)

        val muteState = MainUiState(downloadConfig = DownloadConfig(audioOnly = false, muteAudio = true))
        assertEquals(DownloadMode.MUTE, muteState.downloadMode)
    }

    @Test
    fun stateCopyPreservesImmutabilityAndUpdatesProperties() {
        val initialState = MainUiState()

        val updatedUrlState = initialState.copy(
            url = "https://youtu.be/dQw4w9WgXcQ",
            detectedPlatform = PlatformType.YOUTUBE,
            isUrlValid = true
        )

        assertEquals("https://youtu.be/dQw4w9WgXcQ", updatedUrlState.url)
        assertEquals(PlatformType.YOUTUBE, updatedUrlState.detectedPlatform)
        assertTrue(updatedUrlState.isUrlValid)

        val amoledState = updatedUrlState.copy(isAmoledTheme = true)
        assertTrue(amoledState.isAmoledTheme)
        assertEquals(updatedUrlState.url, amoledState.url)

        val config = DownloadConfig(
            quality = VideoQuality.Q_1080P,
            audioOnly = true,
            audioCodec = AudioCodec.OPUS,
            audioQuality = AudioQuality.HIGH
        )
        val withConfig = amoledState.copy(downloadConfig = config)
        assertEquals(DownloadMode.AUDIO, withConfig.downloadMode)
        assertEquals(AudioCodec.OPUS, withConfig.downloadConfig.audioCodec)
    }

    @Test
    fun downloadStateTransitionsMaintainIntegrity() {
        val initialState = MainUiState()

        val downloadingState = initialState.copy(
            downloadState = DownloadState.Downloading(
                progress = 55.5f,
                speed = "3.2 MB/s",
                eta = "00:10"
            )
        )
        assertTrue(downloadingState.downloadState is DownloadState.Downloading)
        val downloading = downloadingState.downloadState as DownloadState.Downloading
        assertEquals(55.5f, downloading.progress, 0.01f)
        assertEquals("3.2 MB/s", downloading.speed)
        assertEquals("00:10", downloading.eta)

        val errorState = downloadingState.copy(
            downloadState = DownloadState.Error("Network timeout")
        )
        assertTrue(errorState.downloadState is DownloadState.Error)
        assertEquals("Network timeout", (errorState.downloadState as DownloadState.Error).message)

        val dismissedState = errorState.copy(downloadState = DownloadState.Idle)
        assertEquals(DownloadState.Idle, dismissedState.downloadState)
    }
}
