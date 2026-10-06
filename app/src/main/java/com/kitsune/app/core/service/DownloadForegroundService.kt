package com.kitsune.app.core.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import com.kitsune.app.R
import com.kitsune.app.core.engine.YtDlpEngine
import com.kitsune.app.core.model.AudioCodec
import com.kitsune.app.core.model.AudioQuality
import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.DownloadStage
import com.kitsune.app.core.model.DownloadState
import com.kitsune.app.core.model.DownloadTask
import com.kitsune.app.core.model.VideoQuality
import com.kitsune.app.core.storage.MediaStoreExporter
import com.kitsune.app.core.storage.UserPreferencesRepository
import com.kitsune.app.core.util.NetworkMonitor
import com.kitsune.app.core.util.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class DownloadForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var activeJob: Job? = null
    private var networkJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var networkMonitor: NetworkMonitor

    companion object {
        const val ACTION_START_DOWNLOAD = "com.kitsune.app.action.START_DOWNLOAD"
        const val ACTION_ENQUEUE_BATCH = "com.kitsune.app.action.ENQUEUE_BATCH"
        const val ACTION_CANCEL_DOWNLOAD = "com.kitsune.app.action.CANCEL_DOWNLOAD"
        const val ACTION_CANCEL_TASK = "com.kitsune.app.action.CANCEL_TASK"

        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_QUALITY = "extra_quality"
        const val EXTRA_AUDIO_ONLY = "extra_audio_only"
        const val EXTRA_AUDIO_CODEC = "extra_audio_codec"
        const val EXTRA_AUDIO_QUALITY = "extra_audio_quality"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_URLS = "extra_urls"
        const val EXTRA_TITLES = "extra_titles"

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
                putExtra(EXTRA_QUALITY, config.quality.name)
                putExtra(EXTRA_AUDIO_ONLY, config.audioOnly)
                putExtra(EXTRA_AUDIO_CODEC, config.audioCodec.name)
                putExtra(EXTRA_AUDIO_QUALITY, config.audioQuality.name)
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
                putExtra(EXTRA_QUALITY, config.quality.name)
                putExtra(EXTRA_AUDIO_ONLY, config.audioOnly)
                putExtra(EXTRA_AUDIO_CODEC, config.audioCodec.name)
                putExtra(EXTRA_AUDIO_QUALITY, config.audioQuality.name)
            }
            startServiceCompat(context, intent)
        }

        fun cancelDownload(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
            }
            context.startService(intent)
        }

        fun cancelTask(context: Context, taskId: String) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_CANCEL_TASK
                putExtra(EXTRA_TASK_ID, taskId)
            }
            context.startService(intent)
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
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val url = intent.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
                val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { url }
                val config = parseConfigFromIntent(intent)
                val task = DownloadTask(
                    id = UUID.randomUUID().toString(),
                    url = url,
                    title = title,
                    config = config
                )
                enqueueTask(task)
            }
            ACTION_ENQUEUE_BATCH -> {
                val urls = intent.getStringArrayListExtra(EXTRA_URLS) ?: return START_NOT_STICKY
                val titles = intent.getStringArrayListExtra(EXTRA_TITLES) ?: arrayListOf()
                val config = parseConfigFromIntent(intent)
                val newTasks = urls.mapIndexed { index, u ->
                    val t = titles.getOrNull(index).orEmpty().ifBlank { u }
                    DownloadTask(
                        id = UUID.randomUUID().toString(),
                        url = u,
                        title = t,
                        config = config
                    )
                }
                enqueueBatchTasks(newTasks)
            }
            ACTION_CANCEL_TASK -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return START_NOT_STICKY
                handleCancelTask(taskId)
            }
            ACTION_CANCEL_DOWNLOAD -> {
                handleCancelAll()
            }
        }
        return START_NOT_STICKY
    }

    private fun parseConfigFromIntent(intent: Intent): DownloadConfig {
        val qualityName = intent.getStringExtra(EXTRA_QUALITY) ?: VideoQuality.AUTO.name
        val audioOnly = intent.getBooleanExtra(EXTRA_AUDIO_ONLY, false)
        val codecName = intent.getStringExtra(EXTRA_AUDIO_CODEC) ?: AudioCodec.MP3.name
        val audioQualityName = intent.getStringExtra(EXTRA_AUDIO_QUALITY) ?: AudioQuality.BEST.name

        return DownloadConfig(
            quality = runCatching { VideoQuality.valueOf(qualityName) }.getOrDefault(VideoQuality.AUTO),
            audioOnly = audioOnly,
            audioCodec = runCatching { AudioCodec.valueOf(codecName) }.getOrDefault(AudioCodec.MP3),
            audioQuality = runCatching { AudioQuality.valueOf(audioQualityName) }.getOrDefault(AudioQuality.BEST)
        )
    }

    private fun enqueueTask(task: DownloadTask) {
        _downloadQueue.update { it + task }
        triggerQueueProcessing()
    }

    private fun enqueueBatchTasks(tasks: List<DownloadTask>) {
        _downloadQueue.update { it + tasks }
        triggerQueueProcessing()
    }

    private fun triggerQueueProcessing() {
        if (activeJob?.isActive == true) return
        processNextInQueue()
    }

    private fun processNextInQueue() {
        val currentQueue = _downloadQueue.value
        val nextTask = currentQueue.firstOrNull { it.state == DownloadState.Idle || it.state == DownloadState.WaitingForWifi }

        if (nextTask == null) {
            _activeTask.value = null
            cleanupAndStop()
            return
        }

        _activeTask.value = nextTask
        wakeLock?.acquire(60 * 60 * 1000L)

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

        activeJob = serviceScope.launch {
            val wifiOnly = preferencesRepository.isWifiOnlyFlow.firstOrNull() ?: false
            if (wifiOnly && !networkMonitor.isWifiConnected()) {
                updateTaskState(nextTask.id, DownloadState.WaitingForWifi)
                _currentDownloadState.value = DownloadState.WaitingForWifi
                updateNotificationWaitingForWifi()
                waitForWifiAndResume(nextTask)
                return@launch
            }

            executeTaskDownload(nextTask)
        }
    }

    private fun updateNotificationWaitingForWifi() {
        val notif = DownloadNotificationHelper.buildProgressNotification(
            context = this,
            title = getString(R.string.app_name),
            statusText = getString(R.string.status_waiting_wifi),
            progressPercent = 0,
            isIndeterminate = true
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(DownloadNotificationHelper.NOTIFICATION_ID, notif)
    }

    private fun waitForWifiAndResume(task: DownloadTask) {
        networkJob?.cancel()
        networkJob = serviceScope.launch {
            networkMonitor.networkStatusFlow.collect { status ->
                if (status == NetworkStatus.WIFI) {
                    networkJob?.cancel()
                    executeTaskDownload(task)
                }
            }
        }
    }

    private suspend fun executeTaskDownload(task: DownloadTask) {
        val cacheDir = File(cacheDir, "kitsune_tmp")
        val initialDownloadState = DownloadState.Downloading(
            progress = 0f,
            stage = DownloadStage.INITIALIZING
        )
        updateTaskState(task.id, initialDownloadState)
        _currentDownloadState.value = initialDownloadState

        val totalInQueue = _downloadQueue.value.size
        val pendingIndex = _downloadQueue.value.indexOfFirst { it.id == task.id } + 1

        val result = YtDlpEngine.executeDownload(
            context = this@DownloadForegroundService,
            url = task.url,
            config = task.config,
            outputDir = cacheDir
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
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.notify(DownloadNotificationHelper.NOTIFICATION_ID, notif)
        }

        result.fold(
            onSuccess = { downloadedFile ->
                val muxingState = DownloadState.Muxing(getString(R.string.stage_finalizing))
                updateTaskState(task.id, muxingState)
                _currentDownloadState.value = muxingState

                val exportResult = MediaStoreExporter.exportToGallery(
                    context = this@DownloadForegroundService,
                    sourceFile = downloadedFile,
                    title = downloadedFile.nameWithoutExtension,
                    isAudioOnly = task.config.audioOnly
                )

                exportResult.fold(
                    onSuccess = {
                        val fileSize = downloadedFile.length()
                        val formattedSize = if (fileSize > 1024 * 1024) {
                            "%.1f MB".format(fileSize / (1024.0 * 1024.0))
                        } else {
                            "%.1f KB".format(fileSize / 1024.0)
                        }

                        val completedState = DownloadState.Completed(
                            title = downloadedFile.nameWithoutExtension,
                            outputPath = if (task.config.audioOnly) "Music/Kitsune" else "Movies/Kitsune",
                            fileSizeFormatted = formattedSize
                        )
                        updateTaskState(task.id, completedState)
                        _currentDownloadState.value = completedState

                        val completedNotif = DownloadNotificationHelper.buildCompletedNotification(
                            context = this@DownloadForegroundService,
                            title = downloadedFile.nameWithoutExtension,
                            subtext = getString(R.string.status_saved_gallery),
                            targetUri = exportResult.getOrNull(),
                            isAudioOnly = task.config.audioOnly
                        )
                        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        manager.notify(DownloadNotificationHelper.NOTIFICATION_ID + pendingIndex, completedNotif)
                    },
                    onFailure = { err ->
                        val errState = DownloadState.Error(err.localizedMessage ?: getString(R.string.error_export_generic))
                        updateTaskState(task.id, errState)
                        _currentDownloadState.value = errState
                    }
                )
            },
            onFailure = { error ->
                val errState = DownloadState.Error(error.localizedMessage ?: getString(R.string.error_download_generic))
                updateTaskState(task.id, errState)
                _currentDownloadState.value = errState
            }
        )

        removeCompletedTaskAfterDelay(task.id)
        processNextInQueue()
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

    private fun removeCompletedTaskAfterDelay(taskId: String) {
        serviceScope.launch {
            delay(1500)
            _downloadQueue.update { list -> list.filterNot { it.id == taskId } }
        }
    }

    private fun handleCancelTask(taskId: String) {
        if (_activeTask.value?.id == taskId) {
            activeJob?.cancel()
            networkJob?.cancel()
            _downloadQueue.update { list -> list.filterNot { it.id == taskId } }
            _activeTask.value = null
            _currentDownloadState.value = DownloadState.Idle
            processNextInQueue()
        } else {
            _downloadQueue.update { list -> list.filterNot { it.id == taskId } }
        }
    }

    private fun handleCancelAll() {
        activeJob?.cancel()
        networkJob?.cancel()
        _downloadQueue.value = emptyList()
        _activeTask.value = null
        _currentDownloadState.value = DownloadState.Idle
        cleanupAndStop()
    }

    private fun cleanupAndStop() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        activeJob?.cancel()
        networkJob?.cancel()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
}
