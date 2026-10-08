package com.kitsune.app.domain.repository

import com.kitsune.app.domain.model.DownloadState
import com.kitsune.app.domain.model.DownloadTask
import kotlinx.coroutines.flow.StateFlow

interface DownloadQueueRepository {
    val queue: StateFlow<List<DownloadTask>>
    val currentState: StateFlow<DownloadState>
    val activeTask: StateFlow<DownloadTask?>

    fun enqueue(tasks: List<DownloadTask>)
    fun nextPendingTask(): DownloadTask?
    fun setActiveTask(task: DownloadTask?)
    fun updateTaskState(taskId: String, state: DownloadState)
    fun reportState(taskId: String, state: DownloadState)
    fun remove(taskId: String)
    fun clear()
    fun onProcessingStopped()
    fun resetCurrentState()
}
