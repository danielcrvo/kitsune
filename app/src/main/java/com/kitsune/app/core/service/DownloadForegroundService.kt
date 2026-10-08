package com.kitsune.app.core.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ServiceCompat
import com.kitsune.app.R
import com.kitsune.app.core.engine.YtDlpEngine
import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.DownloadStage
import com.kitsune.app.core.model.DownloadState
import com.kitsune.app.core.model.DownloadTask
import com.kitsune.app.core.storage.MediaStoreExporter
import com.kitsune.app.core.storage.UserPreferencesRepository
import com.kitsune.app.core.util.NetworkMonitor
import com.kitsune.app.core.util.NetworkStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class DownloadForegroundService : Service() {

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main + CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Unexpected failure while processing the download queue", throwable)
        }
    )
    private var activeJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isForeground = false
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var networkMonitor: NetworkMonitor

    companion object {
        const val ACTION_START_DOWNLOAD = "com.kitsune.app.action.START_DOWNLOAD"
        const val ACTION_ENQUEUE_BATCH = "com.kitsune.app.action.ENQUEUE_BATCH"
        const val ACTION_CANCEL_DOWNLOAD = "com.kitsune.app.action.CANCEL_DOWNLOAD"
        const val ACTION_CANCEL_TASK = "com.kitsune.app.action.CANCEL_TASK"

        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CONFIG = "extra_config"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_URLS = "extra_urls"
        const val EXTRA_TITLES = "extra_titles"

        private const val TAG = "DownloadService"
        private const val TEMP_DIR_NAME = "kitsune_tmp"
        private const val WAKE_LOCK_TIMEOUT_MS = 60 * 60 * 1000L
        private const val COMPLETED_NOTIFICATION_BASE_ID = 3000

        private val completedNotificationIds = AtomicInteger(COMPLETED_NOTIFICATION_BASE_ID)

        private val _currentDownloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
        val currentDownloadState: StateFlow<DownloadState> = _currentDownloadState.asStateFlow()

        private val _downloadQueue = MutableStateFlow<List<DownloadTask>>(emptyList())
        val downloadQueue: StateFlow<List<DownloadTask>> = _downloadQueue.asStateFlow()

        private val _activeTask = MutableStateFlow<DownloadTask?>(null)
        val activeTask: StateFlow<DownloadTask?> = _activeTask.asStateFlow()

        fun startDownload(
            context: Context,
            url: String,
            config: DownloadConfig,
            title: String = ""
        ) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_CONFIG, config.encode())
            }
            startServiceCompat(context, intent)
        }

        fun enqueueBatch(
            context: Context,
            urls: ArrayList<String>,
            titles: ArrayList<String>,
            config: DownloadConfig
        ) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_ENQUEUE_BATCH
                putStringArrayListExtra(EXTRA_URLS, urls)
                putStringArrayListExtra(EXTRA_TITLES, titles)
                putExtra(EXTRA_CONFIG, config.encode())
            }
            startServiceCompat(context, intent)
        }

        fun cancelDownload(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
            }
            runCatching { context.startService(intent) }
        }

        fun cancelTask(context: Context, taskId: String) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_CANCEL_TASK
                putExtra(EXTRA_TASK_ID, taskId)
            }
            runCatching { context.startService(intent) }
        }

        private fun startServiceCompat(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        preferencesRepository = UserPreferencesRepository(this)
        networkMonitor = NetworkMonitor(this)
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "kitsune:download_wakelock"
        ).apply { setReferenceCounted(false) }

        if (_activeTask.value == null) {
            File(cacheDir, TEMP_DIR_NAME).deleteRecursively()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                ensureForeground()
                val url = intent.getStringExtra(EXTRA_URL)
                if (url.isNullOrBlank()) {
                    processNextInQueue()
                    return START_NOT_STICKY
                }
                val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { url }
                val config = DownloadConfig.decode(intent.getStringExtra(EXTRA_CONFIG))
                val task = DownloadTask(
                    id = UUID.randomUUID().toString(),
                    url = url,
                    title = title,
                    config = config
                )
                enqueueTasks(listOf(task))
            }
            ACTION_ENQUEUE_BATCH -> {
                ensureForeground()
                val urls = intent.getStringArrayListExtra(EXTRA_URLS).orEmpty()
                val titles = intent.getStringArrayListExtra(EXTRA_TITLES).orEmpty()
                val config = DownloadConfig.decode(intent.getStringExtra(EXTRA_CONFIG))
                val newTasks = urls.mapIndexed { index, u ->
                    DownloadTask(
                        id = UUID.randomUUID().toString(),
                        url = u,
                        title = titles.getOrNull(index).orEmpty().ifBlank { u },
                        config = config
                    )
                }
                enqueueTasks(newTasks)
            }
            ACTION_CANCEL_TASK -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID)
                if (taskId != null) handleCancelTask(taskId)
            }
            ACTION_CANCEL_DOWNLOAD -> {
                handleCancelAll()
            }
            else -> {
                if (activeJob?.isActive != true) cleanupAndStop()
            }
        }
        return START_NOT_STICKY
    }

    private fun enqueueTasks(tasks: List<DownloadTask>) {
        if (tasks.isNotEmpty()) {
            _downloadQueue.update { it + tasks }
        }
        processNextInQueue()
    }

    private fun processNextInQueue() {
        if (activeJob?.isActive == true) return

        val nextTask = _downloadQueue.value.firstOrNull {
            it.state == DownloadState.Idle || it.state == DownloadState.WaitingForWifi
        }

        if (nextTask == null) {
            _activeTask.value = null
            cleanupAndStop()
            return
        }

        _activeTask.value = nextTask
        ensureForeground()

        val job = serviceScope.launch {
            val wifiOnly = preferencesRepository.isWifiOnlyFlow.first()
            if (wifiOnly && !networkMonitor.isWifiConnected()) {
                updateTaskState(nextTask.id, DownloadState.WaitingForWifi)
                _currentDownloadState.value = DownloadState.WaitingForWifi
                updateNotificationWaitingForWifi()
                releaseWakeLock()
                networkMonitor.networkStatusFlow.first { it == NetworkStatus.WIFI }
            }

            acquireWakeLock()
            executeTaskDownload(nextTask)
        }
        activeJob = job
        job.invokeOnCompletion { cause ->
            if (cause !is CancellationException) {
                serviceScope.launch { processNextInQueue() }
            }
        }
    }

    private fun ensureForeground() {
        if (isForeground) return
        val initialNotif = DownloadNotificationHelper.buildProgressNotification(
            context = this,
            title = getString(R.string.app_name),
            statusText = getString(R.string.stage_initializing),
            progressPercent = 0,
            isIndeterminate = true
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                DownloadNotificationHelper.NOTIFICATION_ID,
                initialNotif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(DownloadNotificationHelper.NOTIFICATION_ID, initialNotif)
        }
        isForeground = true
    }

    private fun notificationManager(): NotificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun updateNotificationWaitingForWifi() {
        val notif = DownloadNotificationHelper.buildProgressNotification(
            context = this,
            title = getString(R.string.app_name),
            statusText = getString(R.string.status_waiting_wifi),
            progressPercent = 0,
            isIndeterminate = true
        )
        notificationManager().notify(DownloadNotificationHelper.NOTIFICATION_ID, notif)
    }

    private suspend fun executeTaskDownload(task: DownloadTask) {
        val taskDir = File(File(cacheDir, TEMP_DIR_NAME), task.id)
        val initialDownloadState = DownloadState.Downloading(
            progress = 0f,
            stage = DownloadStage.INITIALIZING
        )
        updateTaskState(task.id, initialDownloadState)
        _currentDownloadState.value = initialDownloadState

        val queueSnapshot = _downloadQueue.value
        val totalInQueue = queueSnapshot.size
        val pendingIndex = queueSnapshot.indexOfFirst { it.id == task.id } + 1
        var lastNotifiedPercent = -1
        var lastNotifiedStage: DownloadStage? = null

        try {
            val result = YtDlpEngine.executeDownload(
                context = this@DownloadForegroundService,
                url = task.url,
                config = task.config,
                outputDir = taskDir
            ) { progress, speed, eta, stage ->
                val downloadingState = DownloadState.Downloading(
                    progress = progress,
                    speed = speed,
                    eta = eta,
                    stage = stage
                )
                updateTaskProgress(task.id, downloadingState, progress, speed, eta)
                _currentDownloadState.value = downloadingState

                val progressInt = (progress * 100).toInt().coerceIn(0, 100)
                if (progressInt == lastNotifiedPercent && stage == lastNotifiedStage) {
                    return@executeDownload
                }
                lastNotifiedPercent = progressInt
                lastNotifiedStage = stage

                val headerTitle = if (totalInQueue > 1) {
                    getString(R.string.notif_queue_progress, pendingIndex, totalInQueue)
                } else {
                    "${getString(R.string.app_name)}: ${getString(R.string.notif_downloading_media)}"
                }

                val notif = DownloadNotificationHelper.buildProgressNotification(
                    context = this@DownloadForegroundService,
                    title = headerTitle,
                    statusText = "${getString(stage.labelRes)} $speed $eta".trim(),
                    progressPercent = progressInt,
                    isIndeterminate = progressInt <= 0
                )
                notificationManager().notify(DownloadNotificationHelper.NOTIFICATION_ID, notif)
            }

            val downloadedFile = result.getOrElse { error ->
                setTaskError(task.id, error.localizedMessage ?: getString(R.string.error_download_generic))
                return
            }

            val muxingState = DownloadState.Muxing(getString(R.string.stage_finalizing))
            updateTaskState(task.id, muxingState)
            _currentDownloadState.value = muxingState

            val exportTitle = task.title.takeIf { it.isNotBlank() && it != task.url }
                ?: downloadedFile.nameWithoutExtension

            val exported = MediaStoreExporter.exportToGallery(
                context = this@DownloadForegroundService,
                sourceFile = downloadedFile,
                title = exportTitle,
                isAudioOnly = task.config.audioOnly
            ).getOrElse { error ->
                setTaskError(task.id, error.localizedMessage ?: getString(R.string.error_export_generic))
                return
            }

            val fileSize = exported.sizeBytes
            val formattedSize = if (fileSize > 1024 * 1024) {
                "%.1f MB".format(fileSize / (1024.0 * 1024.0))
            } else {
                "%.1f KB".format(fileSize / 1024.0)
            }

            val completedState = DownloadState.Completed(
                title = exported.displayName,
                outputPath = if (task.config.audioOnly) "Music/Kitsune" else "Movies/Kitsune",
                fileSizeFormatted = formattedSize
            )
            updateTaskState(task.id, completedState)
            _currentDownloadState.value = completedState

            val completedNotif = DownloadNotificationHelper.buildCompletedNotification(
                context = this@DownloadForegroundService,
                title = exported.displayName,
                subtext = getString(R.string.status_saved_gallery),
                targetUri = exported.uri,
                isAudioOnly = task.config.audioOnly
            )
            notificationManager().notify(completedNotificationIds.incrementAndGet(), completedNotif)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (t: Throwable) {
            setTaskError(task.id, t.localizedMessage ?: getString(R.string.error_download_generic))
        } finally {
            withContext(NonCancellable + Dispatchers.IO) {
                taskDir.deleteRecursively()
                File(taskDir.parentFile, "${taskDir.name}.path").delete()
            }
            removeTaskAfterDelay(task.id)
        }
    }

    private fun setTaskError(taskId: String, message: String) {
        val errState = DownloadState.Error(message)
        updateTaskState(taskId, errState)
        _currentDownloadState.value = errState
    }

    private fun updateTaskState(taskId: String, state: DownloadState) {
        _downloadQueue.update { list ->
            list.map { if (it.id == taskId) it.copy(state = state) else it }
        }
    }

    private fun updateTaskProgress(taskId: String, state: DownloadState, progress: Float, speed: String, eta: String) {
        _downloadQueue.update { list ->
            list.map {
                if (it.id == taskId) {
                    it.copy(state = state, progress = progress, speed = speed, eta = eta)
                } else it
            }
        }
    }

    private fun removeTaskAfterDelay(taskId: String) {
        serviceScope.launch {
            delay(1500)
            _downloadQueue.update { list -> list.filterNot { it.id == taskId } }
        }
    }

    private fun handleCancelTask(taskId: String) {
        _downloadQueue.update { list -> list.filterNot { it.id == taskId } }
        if (_activeTask.value?.id == taskId) {
            activeJob?.cancel()
            activeJob = null
            _activeTask.value = null
            _currentDownloadState.value = DownloadState.Idle
            processNextInQueue()
        } else if (activeJob?.isActive != true) {
            processNextInQueue()
        }
    }

    private fun handleCancelAll() {
        activeJob?.cancel()
        activeJob = null
        _downloadQueue.value = emptyList()
        _activeTask.value = null
        _currentDownloadState.value = DownloadState.Idle
        cleanupAndStop()
    }

    private fun acquireWakeLock() {
        wakeLock?.acquire(WAKE_LOCK_TIMEOUT_MS)
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
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
        _activeTask.value = null
        _downloadQueue.value = emptyList()
        when (_currentDownloadState.value) {
            is DownloadState.Downloading,
            is DownloadState.Muxing,
            DownloadState.WaitingForWifi -> _currentDownloadState.value = DownloadState.Idle
            else -> {}
        }
        super.onDestroy()
    }
}
