package com.kitsune.app.data.download

import android.content.Context
import android.content.Intent
import android.util.Log
import com.kitsune.app.domain.repository.DownloadScheduler
import com.kitsune.app.service.DownloadForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceDownloadScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : DownloadScheduler {

    override fun startProcessing() {
        runCatching {
            context.startForegroundService(serviceIntent(DownloadForegroundService.ACTION_PROCESS_QUEUE))
        }.onFailure { Log.e(TAG, "Could not start the download service", it) }
    }

    override fun cancelTask(taskId: String) {
        val intent = serviceIntent(DownloadForegroundService.ACTION_CANCEL_TASK).apply {
            putExtra(DownloadForegroundService.EXTRA_TASK_ID, taskId)
        }
        runCatching { context.startService(intent) }
    }

    override fun cancelAll() {
        runCatching { context.startService(serviceIntent(DownloadForegroundService.ACTION_CANCEL_ALL)) }
    }

    private companion object {
        const val TAG = "DownloadScheduler"
    }

    private fun serviceIntent(action: String): Intent =
        Intent(context, DownloadForegroundService::class.java).setAction(action)
}
