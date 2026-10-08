package com.kitsune.app.domain.usecase

import com.kitsune.app.domain.model.DownloadProgress
import com.kitsune.app.domain.model.DownloadTask
import com.kitsune.app.domain.model.ExportedMedia
import com.kitsune.app.domain.repository.MediaEngine
import com.kitsune.app.domain.repository.MediaExporter
import java.io.File
import javax.inject.Inject

sealed interface DownloadOutcome {
    data class Success(val media: ExportedMedia) : DownloadOutcome
    data class DownloadFailed(val cause: Throwable) : DownloadOutcome
    data class ExportFailed(val cause: Throwable) : DownloadOutcome
}

class ExecuteDownloadTaskUseCase @Inject constructor(
    private val mediaEngine: MediaEngine,
    private val mediaExporter: MediaExporter
) {

    suspend operator fun invoke(
        task: DownloadTask,
        workDir: File,
        onProgress: (DownloadProgress) -> Unit,
        onExporting: () -> Unit
    ): DownloadOutcome {
        val downloadedFile = mediaEngine.download(task.url, task.config, workDir, onProgress)
            .getOrElse { return DownloadOutcome.DownloadFailed(it) }

        onExporting()

        val exportTitle = task.title.takeIf { it.isNotBlank() && it != task.url }
            ?: downloadedFile.nameWithoutExtension

        return mediaExporter.export(downloadedFile, exportTitle, task.config.audioOnly).fold(
            onSuccess = { DownloadOutcome.Success(it) },
            onFailure = { DownloadOutcome.ExportFailed(it) }
        )
    }
}
