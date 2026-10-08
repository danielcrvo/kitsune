package com.kitsune.app.ui.download

import com.kitsune.app.domain.model.AudioCodec
import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.model.DownloadMode
import com.kitsune.app.domain.model.DownloadState
import com.kitsune.app.domain.model.DownloadTask
import com.kitsune.app.domain.model.PlatformType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadUiStateTest {

    @Test
    fun defaultStateHasExpectedInitialValues() {
        val state = DownloadUiState()

        assertEquals("", state.url)
        assertEquals(PlatformType.UNKNOWN, state.detectedPlatform)
        assertFalse(state.isUrlValid)
        assertNull(state.mediaInfo)
        assertNull(state.detectedClipboardUrl)
        assertEquals(DownloadState.Idle, state.downloadState)
        assertEquals(DownloadMode.AUTO, state.downloadMode)
        assertTrue(state.downloadQueue.isEmpty())
        assertNull(state.playlistInfo)
        assertFalse(state.isPlaylistDialogOpen)
        assertFalse(state.isResolvingLink)
        assertFalse(state.hasActiveOrQueuedDownloads)
    }

    @Test
    fun downloadModeResolvesFromConfig() {
        assertEquals(DownloadMode.AUDIO, DownloadUiState(downloadConfig = DownloadConfig(audioOnly = true)).downloadMode)
        assertEquals(DownloadMode.MUTE, DownloadUiState(downloadConfig = DownloadConfig(muteAudio = true)).downloadMode)
        val opus = DownloadUiState(downloadConfig = DownloadConfig(audioOnly = true, audioCodec = AudioCodec.OPUS))
        assertEquals(AudioCodec.OPUS, opus.downloadConfig.audioCodec)
    }

    @Test
    fun resolvingLinkCoversMetadataAndPlaylistLoading() {
        assertTrue(DownloadUiState(isLoadingMetadata = true).isResolvingLink)
        assertTrue(DownloadUiState(isLoadingPlaylist = true).isResolvingLink)
    }

    @Test
    fun activeOrQueuedDownloadsAreDetected() {
        val queued = DownloadTask(url = "https://example.com/1", title = "1", config = DownloadConfig())
        assertTrue(DownloadUiState(downloadQueue = listOf(queued)).hasActiveOrQueuedDownloads)
        assertTrue(DownloadUiState(downloadState = DownloadState.Error("x")).hasActiveOrQueuedDownloads)

        val finished = queued.copy(state = DownloadState.Error("x"))
        assertFalse(DownloadUiState(downloadQueue = listOf(finished)).hasActiveOrQueuedDownloads)
    }
}
