package com.kitsune.app.domain.repository

interface DownloadScheduler {
    fun startProcessing()
    fun cancelTask(taskId: String)
    fun cancelAll()
}
