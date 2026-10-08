package com.kitsune.app.domain.usecase

import com.kitsune.app.domain.repository.DownloadQueueRepository
import com.kitsune.app.domain.repository.DownloadScheduler
import javax.inject.Inject

class CancelDownloadsUseCase @Inject constructor(
    private val queueRepository: DownloadQueueRepository,
    private val scheduler: DownloadScheduler
) {

    fun cancelTask(taskId: String) {
        queueRepository.remove(taskId)
        scheduler.cancelTask(taskId)
    }

    fun cancelAll() {
        queueRepository.clear()
        scheduler.cancelAll()
    }
}
