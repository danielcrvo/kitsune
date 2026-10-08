package com.kitsune.app.ui.download

import com.kitsune.app.data.download.InMemoryDownloadQueueRepository
import com.kitsune.app.domain.model.AudioCodec
import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.model.DownloadMode
import com.kitsune.app.domain.model.DownloadState
import com.kitsune.app.domain.model.MediaInfo
import com.kitsune.app.domain.model.PlatformType
import com.kitsune.app.domain.model.PlaylistInfo
import com.kitsune.app.domain.model.PlaylistItem
import com.kitsune.app.domain.usecase.AnalyzeLinkUseCase
import com.kitsune.app.domain.usecase.CancelDownloadsUseCase
import com.kitsune.app.domain.usecase.EnqueueDownloadsUseCase
import com.kitsune.app.domain.usecase.LoadDownloadConfigUseCase
import com.kitsune.app.testing.FakeDownloadScheduler
import com.kitsune.app.testing.FakeMediaEngine
import com.kitsune.app.testing.FakePreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DownloadViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val engine = FakeMediaEngine()
    private val queue = InMemoryDownloadQueueRepository()
    private val scheduler = FakeDownloadScheduler()
    private lateinit var preferences: FakePreferencesRepository

    private val videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
    private val mediaInfo = MediaInfo(
        id = "dQw4w9WgXcQ",
        title = "Never Gonna Give You Up",
        uploader = "Rick Astley",
        durationSeconds = 213,
        thumbnailUrl = null,
        platform = PlatformType.YOUTUBE,
        originalUrl = videoUrl
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        preferences = FakePreferencesRepository(
            config = DownloadConfig(audioCodec = AudioCodec.OPUS),
            mode = DownloadMode.AUDIO
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.createViewModel(): DownloadViewModel = DownloadViewModel(
        analyzeLink = AnalyzeLinkUseCase(),
        loadDownloadConfig = LoadDownloadConfigUseCase(preferences),
        enqueueDownloads = EnqueueDownloadsUseCase(queue, scheduler),
        cancelDownloads = CancelDownloadsUseCase(queue, scheduler),
        mediaEngine = engine,
        preferencesRepository = preferences,
        queueRepository = queue
    ).also { advanceUntilIdle() }

    @Test
    fun restoresSavedModeWithoutOverridingCodec() = runTest(dispatcher) {
        val viewModel = createViewModel()

        val config = viewModel.uiState.value.downloadConfig
        assertEquals(DownloadMode.AUDIO, config.mode)
        assertEquals(AudioCodec.OPUS, config.audioCodec)
    }

    @Test
    fun metadataLookupIsDebouncedToTheLastUrl() = runTest(dispatcher) {
        engine.mediaInfoResult = Result.success(mediaInfo)
        val viewModel = createViewModel()

        viewModel.onAction(DownloadUiAction.ChangeUrl("https://www.youtube.com/watch?v=dQw"))
        viewModel.onAction(DownloadUiAction.ChangeUrl(videoUrl))
        assertTrue(viewModel.uiState.value.isLoadingMetadata)
        advanceUntilIdle()

        assertEquals(listOf(videoUrl), engine.fetchedUrls)
        assertEquals(mediaInfo, viewModel.uiState.value.mediaInfo)
        assertFalse(viewModel.uiState.value.isLoadingMetadata)
    }

    @Test
    fun changingUrlClearsStalePreview() = runTest(dispatcher) {
        engine.mediaInfoResult = Result.success(mediaInfo)
        val viewModel = createViewModel()
        viewModel.onAction(DownloadUiAction.ChangeUrl(videoUrl))
        advanceUntilIdle()

        engine.mediaInfoResult = Result.failure(IllegalStateException("offline"))
        viewModel.onAction(DownloadUiAction.ChangeUrl("https://vimeo.com/123"))
        assertNull(viewModel.uiState.value.mediaInfo)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.mediaInfo)
        assertFalse(viewModel.uiState.value.isLoadingMetadata)
    }

    @Test
    fun startDownloadEnqueuesTaskWithTitleAndFullConfig() = runTest(dispatcher) {
        engine.mediaInfoResult = Result.success(mediaInfo)
        val viewModel = createViewModel()
        viewModel.onAction(DownloadUiAction.SetDownloadMode(DownloadMode.MUTE))
        viewModel.onAction(DownloadUiAction.ChangeConfig(viewModel.uiState.value.downloadConfig.copy(embedSubtitles = true)))
        viewModel.onAction(DownloadUiAction.ChangeUrl(videoUrl))
        advanceUntilIdle()

        viewModel.onAction(DownloadUiAction.StartDownload)
        advanceUntilIdle()

        val task = queue.queue.value.single()
        assertEquals(mediaInfo.title, task.title)
        assertTrue(task.config.muteAudio)
        assertTrue(task.config.embedSubtitles)
        assertEquals(1, scheduler.startCount)
        assertEquals(1, viewModel.uiState.value.downloadQueue.size)
        assertEquals(DownloadMode.MUTE, preferences.modeFlow.value)
    }

    @Test
    fun startDownloadIgnoresInvalidUrls() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.onAction(DownloadUiAction.ChangeUrl("not a link"))
        viewModel.onAction(DownloadUiAction.StartDownload)
        advanceUntilIdle()

        assertTrue(queue.queue.value.isEmpty())
        assertEquals(0, scheduler.startCount)
    }

    @Test
    fun clipboardUrlFillsEmptyFieldOrIsOfferedAsSuggestion() = runTest(dispatcher) {
        engine.mediaInfoResult = Result.success(mediaInfo)
        val viewModel = createViewModel()

        viewModel.onAction(DownloadUiAction.ClipboardTextAvailable("  $videoUrl "))
        assertEquals(videoUrl, viewModel.uiState.value.url)

        viewModel.onAction(DownloadUiAction.ClipboardTextAvailable("https://vimeo.com/123"))
        assertEquals("https://vimeo.com/123", viewModel.uiState.value.detectedClipboardUrl)

        viewModel.onAction(DownloadUiAction.ClipboardTextAvailable("just text"))
        assertEquals("https://vimeo.com/123", viewModel.uiState.value.detectedClipboardUrl)
    }

    @Test
    fun playlistSelectionEnqueuesOnlySelectedItems() = runTest(dispatcher) {
        engine.playlistResult = Result.success(
            PlaylistInfo(
                id = "PL1",
                title = "Mix",
                originalUrl = "https://www.youtube.com/playlist?list=PL1",
                items = listOf(
                    PlaylistItem(id = "a", title = "A", url = "https://www.youtube.com/watch?v=a"),
                    PlaylistItem(id = "b", title = "B", url = "https://www.youtube.com/watch?v=b")
                )
            )
        )
        val viewModel = createViewModel()
        viewModel.onAction(DownloadUiAction.ChangeUrl("https://www.youtube.com/playlist?list=PL1"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isPlaylistDialogOpen)
        assertEquals(setOf("a", "b"), viewModel.uiState.value.selectedPlaylistItems)

        viewModel.onAction(DownloadUiAction.TogglePlaylistItem("a"))
        viewModel.onAction(DownloadUiAction.DownloadSelectedPlaylistItems)
        advanceUntilIdle()

        assertEquals(listOf("B"), queue.queue.value.map { it.title })
        assertFalse(viewModel.uiState.value.isPlaylistDialogOpen)
        assertEquals("", viewModel.uiState.value.url)
    }

    @Test
    fun cancellingAndDismissingUpdateQueueState() = runTest(dispatcher) {
        engine.mediaInfoResult = Result.success(mediaInfo)
        val viewModel = createViewModel()
        viewModel.onAction(DownloadUiAction.ChangeUrl(videoUrl))
        advanceUntilIdle()
        viewModel.onAction(DownloadUiAction.StartDownload)
        val taskId = queue.queue.value.single().id

        viewModel.onAction(DownloadUiAction.CancelQueueTask(taskId))
        assertEquals(listOf(taskId), scheduler.cancelledTaskIds)
        assertTrue(queue.queue.value.isEmpty())

        queue.reportState("missing", DownloadState.Error("boom"))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.downloadState is DownloadState.Error)

        viewModel.onAction(DownloadUiAction.DismissError)
        advanceUntilIdle()
        assertEquals(DownloadState.Idle, viewModel.uiState.value.downloadState)
    }
}
