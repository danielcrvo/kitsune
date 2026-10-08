package com.kitsune.app.data.download

import com.kitsune.app.domain.model.DownloadState
import com.kitsune.app.domain.model.DownloadTask
import com.kitsune.app.domain.repository.DownloadQueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryDownloadQueueRepository @Inject constructor() : DownloadQueueRepository {

    private val _queue = MutableStateFlow<List<DownloadTask>>(emptyList())
    override val queue: StateFlow<List<DownloadTask>> = _queue.asStateFlow()

    private val _currentState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    override val currentState: StateFlow<DownloadState> = _currentState.asStateFlow()

    private val _activeTask = MutableStateFlow<DownloadTask?>(null)
    override val activeTask: StateFlow<DownloadTask?> = _activeTask.asStateFlow()

    override fun enqueue(tasks: List<DownloadTask>) {
        if (tasks.isEmpty()) return
        _queue.update { it + tasks }
    }

    override fun nextPendingTask(): DownloadTask? = _queue.value.firstOrNull {
        it.state == DownloadState.Idle || it.state == DownloadState.WaitingForWifi
    }

    override fun setActiveTask(task: DownloadTask?) {
        _activeTask.value = task
    }

    override fun updateTaskState(taskId: String, state: DownloadState) {
        _queue.update { list ->
            list.map { task ->
                if (task.id != taskId) return@map task
                if (state is DownloadState.Downloading) {
                    task.copy(state = state, progress = state.progress, speed = state.speed, eta = state.eta)
                } else {
                    task.copy(state = state)
                }
            }
        }
    }

    override fun reportState(taskId: String, state: DownloadState) {
        updateTaskState(taskId, state)
        _currentState.value = state
    }

    override fun remove(taskId: String) {
        _queue.update { list -> list.filterNot { it.id == taskId } }
        if (_activeTask.value?.id == taskId) {
            _activeTask.value = null
        }
    }

    override fun clear() {
        _queue.value = emptyList()
        _activeTask.value = null
        _currentState.value = DownloadState.Idle
    }

    override fun onProcessingStopped() {
        _queue.update { list ->
            list.filter { it.state == DownloadState.Idle || it.state == DownloadState.WaitingForWifi }
                .map { it.copy(state = DownloadState.Idle) }
        }
        _activeTask.value = null
        when (_currentState.value) {
            is DownloadState.Downloading,
            is DownloadState.Muxing,
            is DownloadState.FetchingInfo,
            DownloadState.WaitingForWifi -> _currentState.value = DownloadState.Idle
            else -> Unit
        }
    }

    override fun resetCurrentState() {
        _currentState.value = DownloadState.Idle
    }
}
