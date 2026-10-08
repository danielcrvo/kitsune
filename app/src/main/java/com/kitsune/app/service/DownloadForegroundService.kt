package com.kitsune.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ServiceCompat
import com.kitsune.app.R
import com.kitsune.app.data.media.MediaFileUtils
import com.kitsune.app.domain.model.DownloadProgress
import com.kitsune.app.domain.model.DownloadStage
import com.kitsune.app.domain.model.DownloadState
import com.kitsune.app.domain.model.DownloadTask
import com.kitsune.app.domain.model.NetworkStatus
import com.kitsune.app.domain.repository.DownloadQueueRepository
import com.kitsune.app.domain.repository.NetworkMonitor
import com.kitsune.app.domain.repository.PreferencesRepository
import com.kitsune.app.domain.usecase.DownloadOutcome
import com.kitsune.app.domain.usecase.ExecuteDownloadTaskUseCase
import com.kitsune.app.domain.util.FileSizeFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class DownloadForegroundService : Service() {

    companion object {
        const val ACTION_PROCESS_QUEUE = "com.kitsune.app.action.PROCESS_QUEUE"
        const val ACTION_CANCEL_ALL = "com.kitsune.app.action.CANCEL_DOWNLOAD"
        const val ACTION_CANCEL_TASK = "com.kitsune.app.action.CANCEL_TASK"
        const val EXTRA_TASK_ID = "extra_task_id"

        private const val TAG = "DownloadService"
        private const val TEMP_DIR_NAME = "kitsune_tmp"
        private const val WAKE_LOCK_TIMEOUT_MS = 60 * 60 * 1000L
        private const val FINISHED_TASK_LINGER_MS = 1500L
    }

    @Inject lateinit var queueRepository: DownloadQueueRepository
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var executeDownloadTask: ExecuteDownloadTaskUseCase
    @Inject lateinit var notifier: KitsuneNotifier

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main + CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Unexpected failure while processing the download queue", throwable)
        }
    )

    private var activeJob: Job? = null
    private var activeTaskId: String? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isForeground = false

    private val tempRoot: File
        get() = File(cacheDir, TEMP_DIR_NAME)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "kitsune:download_wakelock")
            .apply { setReferenceCounted(false) }

        if (queueRepository.activeTask.value == null) {
            tempRoot.deleteRecursively()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PROCESS_QUEUE -> {
                ensureForeground()
                processNextInQueue()
            }
            ACTION_CANCEL_TASK -> intent.getStringExtra(EXTRA_TASK_ID)?.let(::handleCancelTask)
            ACTION_CANCEL_ALL -> handleCancelAll()
            else -> if (activeJob?.isActive != true) cleanupAndStop()
        }
        return START_NOT_STICKY
    }

    private fun processNextInQueue() {
        if (activeJob?.isActive == true) return

        val nextTask = queueRepository.nextPendingTask()
        if (nextTask == null) {
            activeTaskId = null
            queueRepository.setActiveTask(null)
            cleanupAndStop()
            return
        }

        activeTaskId = nextTask.id
        queueRepository.setActiveTask(nextTask)
        ensureForeground()

        val job = serviceScope.launch {
            awaitAllowedNetwork(nextTask)
            acquireWakeLock()
            runTask(nextTask)
        }
        activeJob = job
        job.invokeOnCompletion { cause ->
            if (cause !is CancellationException) {
                serviceScope.launch { processNextInQueue() }
            }
        }
    }

    private suspend fun awaitAllowedNetwork(task: DownloadTask) {
        val wifiOnly = preferencesRepository.isWifiOnly.first()
        if (!wifiOnly || networkMonitor.isWifiConnected()) return

        queueRepository.reportState(task.id, DownloadState.WaitingForWifi)
        notifier.showDownloadProgress(
            title = getString(R.string.app_name),
            statusText = getString(R.string.status_waiting_wifi),
            progressPercent = 0,
            isIndeterminate = true
        )
        releaseWakeLock()
        networkMonitor.networkStatus.first { it == NetworkStatus.WIFI }
    }

    private suspend fun runTask(task: DownloadTask) {
        val workDir = File(tempRoot, task.id)
        queueRepository.reportState(task.id, DownloadState.Downloading(stage = DownloadStage.INITIALIZING))

        val queueSnapshot = queueRepository.queue.value
        val headerTitle = if (queueSnapshot.size > 1) {
            val position = queueSnapshot.indexOfFirst { it.id == task.id } + 1
            getString(R.string.notif_queue_progress, position, queueSnapshot.size)
        } else {
            "${getString(R.string.app_name)}: ${getString(R.string.notif_downloading_media)}"
        }
        val progressNotifier = ProgressNotificationThrottle(headerTitle)

        try {
            val outcome = executeDownloadTask(
                task = task,
                workDir = workDir,
                onProgress = { progress ->
                    queueRepository.reportState(
                        task.id,
                        DownloadState.Downloading(
                            progress = progress.progress,
                            speed = progress.speed,
                            eta = progress.eta,
                            stage = progress.stage
                        )
                    )
                    progressNotifier.onProgress(progress)
                },
                onExporting = {
                    queueRepository.reportState(task.id, DownloadState.Muxing(getString(R.string.stage_finalizing)))
                }
            )
            handleOutcome(task, outcome)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (t: Throwable) {
            reportError(task.id, t, R.string.error_download_generic)
        } finally {
            withContext(NonCancellable + Dispatchers.IO) {
                workDir.deleteRecursively()
                File(tempRoot, "${task.id}.path").delete()
            }
            scheduleRemoval(task.id)
        }
    }

    private fun handleOutcome(task: DownloadTask, outcome: DownloadOutcome) {
        when (outcome) {
            is DownloadOutcome.Success -> {
                val media = outcome.media
                queueRepository.reportState(
                    task.id,
                    DownloadState.Completed(
                        title = media.displayName,
                        outputPath = MediaFileUtils.relativeFolder(task.config.audioOnly),
                        fileSizeFormatted = FileSizeFormatter.format(media.sizeBytes)
                    )
                )
                notifier.showDownloadCompleted(
                    title = media.displayName,
                    subtext = getString(R.string.status_saved_gallery),
                    targetUri = Uri.parse(media.contentUri),
                    isAudioOnly = task.config.audioOnly
                )
            }
            is DownloadOutcome.DownloadFailed -> reportError(task.id, outcome.cause, R.string.error_download_generic)
            is DownloadOutcome.ExportFailed -> reportError(task.id, outcome.cause, R.string.error_export_generic)
        }
    }

    private fun reportError(taskId: String, cause: Throwable, fallbackMessage: Int) {
        val message = cause.localizedMessage?.takeIf { it.isNotBlank() } ?: getString(fallbackMessage)
        queueRepository.reportState(taskId, DownloadState.Error(message))
    }

    private fun scheduleRemoval(taskId: String) {
        serviceScope.launch {
            delay(FINISHED_TASK_LINGER_MS)
            queueRepository.remove(taskId)
        }
    }

    private fun handleCancelTask(taskId: String) {
        queueRepository.remove(taskId)
        if (activeTaskId == taskId) {
            activeJob?.cancel()
            activeJob = null
            activeTaskId = null
            queueRepository.resetCurrentState()
        }
        processNextInQueue()
    }

    private fun handleCancelAll() {
        activeJob?.cancel()
        activeJob = null
        activeTaskId = null
        queueRepository.clear()
        cleanupAndStop()
    }

    private fun ensureForeground() {
        if (isForeground) return
        val notification = notifier.buildDownloadProgress(
            title = getString(R.string.app_name),
            statusText = getString(R.string.stage_initializing),
            progressPercent = 0,
            isIndeterminate = true
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                KitsuneNotifier.DOWNLOAD_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(KitsuneNotifier.DOWNLOAD_NOTIFICATION_ID, notification)
        }
        isForeground = true
    }

    private fun acquireWakeLock() {
        wakeLock?.acquire(WAKE_LOCK_TIMEOUT_MS)
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) wakeLock?.release()
    }

    private fun cleanupAndStop() {
        releaseWakeLock()
        if (isForeground) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            isForeground = false
        }
        stopSelf()
    }

    override fun onDestroy() {
        activeJob?.cancel()
        serviceScope.cancel()
        releaseWakeLock()
        queueRepository.onProcessingStopped()
        super.onDestroy()
    }

    private inner class ProgressNotificationThrottle(private val headerTitle: String) {
        private var lastPercent = -1
        private var lastStage: DownloadStage? = null

        fun onProgress(progress: DownloadProgress) {
            val percent = (progress.progress * 100).toInt().coerceIn(0, 100)
            if (percent == lastPercent && progress.stage == lastStage) return
            lastPercent = percent
            lastStage = progress.stage

            notifier.showDownloadProgress(
                title = headerTitle,
                statusText = "${getString(progress.stage.labelRes)} ${progress.speed} ${progress.eta}".trim(),
                progressPercent = percent,
                isIndeterminate = percent <= 0
            )
        }
    }
}
