package com.kitsune.app.domain.usecase

import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.model.DownloadRequest
import com.kitsune.app.domain.model.DownloadTask
import com.kitsune.app.domain.repository.DownloadQueueRepository
import com.kitsune.app.domain.repository.DownloadScheduler
import javax.inject.Inject

class EnqueueDownloadsUseCase @Inject constructor(
    private val queueRepository: DownloadQueueRepository,
    private val scheduler: DownloadScheduler
) {

    operator fun invoke(requests: List<DownloadRequest>, config: DownloadConfig): List<DownloadTask> {
        val tasks = requests
            .filter { it.url.isNotBlank() }
            .map { request ->
                DownloadTask(
                    url = request.url,
                    title = request.title.ifBlank { request.url },
                    config = config
                )
            }
        if (tasks.isEmpty()) return emptyList()

        queueRepository.enqueue(tasks)
        scheduler.startProcessing()
        return tasks
    }
}
