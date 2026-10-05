package com.kitsune.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadTaskAndPlaylistTest {

    @Test
    fun downloadTaskCreationAndDefaultState() {
        val config = DownloadConfig()
        val task = DownloadTask(
            url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            title = "Never Gonna Give You Up",
            config = config
        )

        assertNotNull(task.id)
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", task.url)
        assertEquals("Never Gonna Give You Up", task.title)
        assertEquals(DownloadState.Idle, task.state)
        assertEquals(0f, task.progress, 0.001f)
        assertEquals("", task.speed)
        assertEquals("", task.eta)
        assertTrue(task.addedAt > 0)
    }

    @Test
    fun downloadTaskStateUpdates() {
        val config = DownloadConfig()
        val task = DownloadTask(
            url = "https://example.com/video.mp4",
            title = "Sample Video",
            config = config
        )

        val waitingTask = task.copy(state = DownloadState.WaitingForWifi)
        assertTrue(waitingTask.state is DownloadState.WaitingForWifi)

        val runningTask = waitingTask.copy(
            state = DownloadState.Downloading(
                progress = 42f,
                speed = "5 MB/s",
                eta = "00:08",
                stage = DownloadStage.VIDEO_STREAM
            ),
            progress = 42f,
            speed = "5 MB/s",
            eta = "00:08"
        )

        assertTrue(runningTask.state is DownloadState.Downloading)
        assertEquals(42f, runningTask.progress, 0.001f)
        assertEquals("5 MB/s", runningTask.speed)
        assertEquals("00:08", runningTask.eta)
    }

    @Test
    fun playlistInfoModelIntegrity() {
        val items = listOf(
            PlaylistItem(id = "1", title = "First Song", url = "https://example.com/song1", durationSeconds = 195L),
            PlaylistItem(id = "2", title = "Second Song", url = "https://example.com/song2", durationSeconds = 260L)
        )
        val playlist = PlaylistInfo(
            id = "PL_test",
            title = "My Playlist",
            originalUrl = "https://example.com/playlist",
            items = items,
            author = "Artist Name"
        )

        assertEquals("PL_test", playlist.id)
        assertEquals("My Playlist", playlist.title)
        assertEquals("https://example.com/playlist", playlist.originalUrl)
        assertEquals("Artist Name", playlist.author)
        assertEquals(2, playlist.items.size)
        assertEquals("First Song", playlist.items[0].title)
        assertEquals("Second Song", playlist.items[1].title)
    }

    @Test
    fun playlistItemSelectionLogic() {
        val items = (1..5).map { index ->
            PlaylistItem(id = "id_$index", title = "Title $index", url = "https://example.com/$index")
        }
        val allIds = items.map { it.id }.toSet()
        assertEquals(5, allIds.size)

        var selected = allIds
        assertEquals(5, selected.size)

        selected = selected - "id_3"
        assertEquals(4, selected.size)
        assertFalse(selected.contains("id_3"))

        selected = selected + "id_3"
        assertEquals(5, selected.size)
        assertTrue(selected.contains("id_3"))

        selected = emptySet()
        assertTrue(selected.isEmpty())
    }
}
