package com.kitsune.app.domain.usecase

import com.kitsune.app.data.download.InMemoryDownloadQueueRepository
import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.model.DownloadRequest
import com.kitsune.app.testing.FakeDownloadScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnqueueDownloadsUseCaseTest {

    @Test
    fun enqueuesTasksAndStartsProcessing() {
        val queue = InMemoryDownloadQueueRepository()
        val scheduler = FakeDownloadScheduler()
        val useCase = EnqueueDownloadsUseCase(queue, scheduler)
        val config = DownloadConfig(muteAudio = true, embedSubtitles = true)

        val tasks = useCase(
            listOf(DownloadRequest("https://example.com/1", "First"), DownloadRequest("https://example.com/2")),
            config
        )

        assertEquals(2, tasks.size)
        assertEquals(listOf("First", "https://example.com/2"), queue.queue.value.map { it.title })
        assertTrue(queue.queue.value.all { it.config == config })
        assertEquals(1, scheduler.startCount)
    }

    @Test
    fun ignoresBlankRequestsWithoutStartingService() {
        val queue = InMemoryDownloadQueueRepository()
        val scheduler = FakeDownloadScheduler()

        val tasks = EnqueueDownloadsUseCase(queue, scheduler)(listOf(DownloadRequest(" ")), DownloadConfig())

        assertTrue(tasks.isEmpty())
        assertTrue(queue.queue.value.isEmpty())
        assertEquals(0, scheduler.startCount)
    }
}
