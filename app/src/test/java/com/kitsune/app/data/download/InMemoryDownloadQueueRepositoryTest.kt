package com.kitsune.app.data.download

import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.model.DownloadStage
import com.kitsune.app.domain.model.DownloadState
import com.kitsune.app.domain.model.DownloadTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryDownloadQueueRepositoryTest {

    private fun task(id: String) = DownloadTask(id = id, url = "https://example.com/$id", title = id, config = DownloadConfig())

    @Test
    fun nextPendingTaskSkipsTasksThatAlreadyStarted() {
        val repository = InMemoryDownloadQueueRepository()
        repository.enqueue(listOf(task("a"), task("b")))
        repository.updateTaskState("a", DownloadState.Downloading())

        assertEquals("b", repository.nextPendingTask()?.id)
    }

    @Test
    fun reportStateUpdatesTaskProgressAndCurrentState() {
        val repository = InMemoryDownloadQueueRepository()
        repository.enqueue(listOf(task("a")))
        val downloading = DownloadState.Downloading(progress = 0.5f, speed = "1MiB/s", eta = "3s", stage = DownloadStage.VIDEO_STREAM)

        repository.reportState("a", downloading)

        val stored = repository.queue.value.single()
        assertEquals(downloading, stored.state)
        assertEquals(0.5f, stored.progress, 0.0001f)
        assertEquals("1MiB/s", stored.speed)
        assertEquals(downloading, repository.currentState.value)
    }

    @Test
    fun removeClearsActiveTaskWhenItMatches() {
        val repository = InMemoryDownloadQueueRepository()
        val active = task("a")
        repository.enqueue(listOf(active))
        repository.setActiveTask(active)

        repository.remove("a")

        assertTrue(repository.queue.value.isEmpty())
        assertNull(repository.activeTask.value)
    }

    @Test
    fun processingStoppedKeepsPendingTasksAndFinalState() {
        val repository = InMemoryDownloadQueueRepository()
        repository.enqueue(listOf(task("done"), task("waiting"), task("pending")))
        repository.reportState("done", DownloadState.Completed("done", "Movies/Kitsune", "1 MB"))
        repository.updateTaskState("waiting", DownloadState.WaitingForWifi)

        repository.onProcessingStopped()

        assertEquals(listOf("waiting", "pending"), repository.queue.value.map { it.id })
        assertTrue(repository.queue.value.all { it.state == DownloadState.Idle })
        assertTrue(repository.currentState.value is DownloadState.Completed)
    }

    @Test
    fun processingStoppedResetsInFlightState() {
        val repository = InMemoryDownloadQueueRepository()
        repository.enqueue(listOf(task("a")))
        repository.reportState("a", DownloadState.Downloading())

        repository.onProcessingStopped()

        assertTrue(repository.queue.value.isEmpty())
        assertEquals(DownloadState.Idle, repository.currentState.value)
    }
}
