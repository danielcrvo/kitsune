package com.kitsune.app.domain.usecase

import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.model.DownloadProgress
import com.kitsune.app.domain.model.DownloadStage
import com.kitsune.app.domain.model.DownloadTask
import com.kitsune.app.domain.model.ExportedMedia
import com.kitsune.app.testing.FakeMediaEngine
import com.kitsune.app.testing.FakeMediaExporter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExecuteDownloadTaskUseCaseTest {

    private val workDir = File("build/tmp/test-work")
    private val downloadedFile = File(workDir, "Some Title [abc].mp4")

    @Test
    fun exportsDownloadedFileUsingTaskTitle() = runTest {
        val engine = FakeMediaEngine().apply {
            downloadResult = Result.success(downloadedFile)
            progressToEmit = listOf(DownloadProgress(0.5f, "1MiB/s", "2s", DownloadStage.VIDEO_STREAM))
        }
        val exporter = FakeMediaExporter { _, title, _ -> Result.success(ExportedMedia("content://media/1", title, 10L)) }
        val progress = mutableListOf<DownloadProgress>()
        var exporting = false

        val outcome = ExecuteDownloadTaskUseCase(engine, exporter)(
            task = DownloadTask(url = "https://example.com/v", title = "Canção", config = DownloadConfig()),
            workDir = workDir,
            onProgress = { progress += it },
            onExporting = { exporting = true }
        )

        assertTrue(outcome is DownloadOutcome.Success)
        assertEquals("Canção", (outcome as DownloadOutcome.Success).media.displayName)
        assertEquals(1, progress.size)
        assertTrue(exporting)
    }

    @Test
    fun fallsBackToFileNameWhenTitleIsTheUrl() = runTest {
        val engine = FakeMediaEngine().apply { downloadResult = Result.success(downloadedFile) }
        val exporter = FakeMediaExporter { _, title, _ -> Result.success(ExportedMedia("content://media/1", title, 10L)) }

        ExecuteDownloadTaskUseCase(engine, exporter)(
            task = DownloadTask(url = "https://example.com/v", title = "https://example.com/v", config = DownloadConfig()),
            workDir = workDir,
            onProgress = {},
            onExporting = {}
        )

        assertEquals(listOf("Some Title [abc]"), exporter.exportedTitles)
    }

    @Test
    fun reportsDownloadFailureWithoutExporting() = runTest {
        val engine = FakeMediaEngine().apply { downloadResult = Result.failure(IllegalStateException("boom")) }
        val exporter = FakeMediaExporter { _, _, _ -> error("should not export") }
        var exporting = false

        val outcome = ExecuteDownloadTaskUseCase(engine, exporter)(
            task = DownloadTask(url = "https://example.com/v", title = "t", config = DownloadConfig()),
            workDir = workDir,
            onProgress = {},
            onExporting = { exporting = true }
        )

        assertTrue(outcome is DownloadOutcome.DownloadFailed)
        assertFalse(exporting)
    }

    @Test
    fun reportsExportFailure() = runTest {
        val engine = FakeMediaEngine().apply { downloadResult = Result.success(downloadedFile) }
        val exporter = FakeMediaExporter { _, _, _ -> Result.failure(IllegalStateException("no space")) }

        val outcome = ExecuteDownloadTaskUseCase(engine, exporter)(
            task = DownloadTask(url = "https://example.com/v", title = "t", config = DownloadConfig()),
            workDir = workDir,
            onProgress = {},
            onExporting = {}
        )

        assertTrue(outcome is DownloadOutcome.ExportFailed)
    }
}
