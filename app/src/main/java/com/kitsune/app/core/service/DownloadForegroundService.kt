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
import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.DownloadStage
import com.kitsune.app.core.model.DownloadState
import com.kitsune.app.core.model.VideoQuality
import com.kitsune.app.core.storage.MediaStoreExporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class DownloadForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var downloadJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val ACTION_START_DOWNLOAD = "com.kitsune.app.action.START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.kitsune.app.action.CANCEL_DOWNLOAD"

        const val EXTRA_URL = "extra_url"
        const val EXTRA_QUALITY = "extra_quality"
        const val EXTRA_AUDIO_ONLY = "extra_audio_only"
        const val EXTRA_AUDIO_CODEC = "extra_audio_codec"
        const val EXTRA_AUDIO_QUALITY = "extra_audio_quality"

        private val _currentDownloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)


        val currentDownloadState: StateFlow<DownloadState> = _currentDownloadState.asStateFlow()


        fun startDownload(
            context: Context,
            url: String,
            config: DownloadConfig
        ) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_QUALITY, config.quality.name)
                putExtra(EXTRA_AUDIO_ONLY, config.audioOnly)
                putExtra(EXTRA_AUDIO_CODEC, config.audioCodec.name)
                putExtra(EXTRA_AUDIO_QUALITY, config.audioQuality.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }


        fun cancelDownload(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
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
                val qualityName = intent.getStringExtra(EXTRA_QUALITY) ?: VideoQuality.AUTO.name
                val audioOnly = intent.getBooleanExtra(EXTRA_AUDIO_ONLY, false)
                val codecName = intent.getStringExtra(EXTRA_AUDIO_CODEC) ?: AudioCodec.MP3.name
                val audioQualityName = intent.getStringExtra(EXTRA_AUDIO_QUALITY) ?: com.kitsune.app.core.model.AudioQuality.BEST.name

                val config = DownloadConfig(
                    quality = runCatching { VideoQuality.valueOf(qualityName) }.getOrDefault(VideoQuality.AUTO),
                    audioOnly = audioOnly,
                    audioCodec = runCatching { AudioCodec.valueOf(codecName) }.getOrDefault(AudioCodec.MP3),
                    audioQuality = runCatching { com.kitsune.app.core.model.AudioQuality.valueOf(audioQualityName) }.getOrDefault(com.kitsune.app.core.model.AudioQuality.BEST)
                )

                handleStartDownload(url, config)
            }
            ACTION_CANCEL_DOWNLOAD -> {
                handleCancelDownload()
            }
        }
        return START_NOT_STICKY
    }

    private fun handleStartDownload(url: String, config: DownloadConfig) {
        downloadJob?.cancel()

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

        downloadJob = serviceScope.launch {
            _currentDownloadState.value = DownloadState.Downloading(
                progress = 0f,
                stage = DownloadStage.INITIALIZING
            )

            val cacheDir = File(cacheDir, "kitsune_tmp")
            val result = YtDlpEngine.executeDownload(
                context = this@DownloadForegroundService,
                url = url,
                config = config,
                outputDir = cacheDir
            ) { progress, speed, eta, stage ->
                _currentDownloadState.value = DownloadState.Downloading(
                    progress = progress,
                    speed = speed,
                    eta = eta,
                    stage = stage
                )

                val progressInt = (progress * 100).toInt().coerceIn(0, 100)
                val notif = DownloadNotificationHelper.buildProgressNotification(
                    context = this@DownloadForegroundService,
                    title = "${getString(R.string.app_name)}: ${getString(R.string.notif_downloading_media)}",
                    statusText = "${getString(stage.labelRes)} $speed $eta".trim(),
                    progressPercent = progressInt,
                    isIndeterminate = progressInt <= 0
                )
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.notify(DownloadNotificationHelper.NOTIFICATION_ID, notif)
            }

            result.fold(
                onSuccess = { downloadedFile ->
                    _currentDownloadState.value = DownloadState.Muxing(getString(R.string.stage_finalizing))
                    val exportResult = MediaStoreExporter.exportToGallery(
                        context = this@DownloadForegroundService,
                        sourceFile = downloadedFile,
                        title = downloadedFile.nameWithoutExtension,
                        isAudioOnly = config.audioOnly
                    )

                    exportResult.fold(
                        onSuccess = {
                            val fileSize = downloadedFile.length()
                            val formattedSize = if (fileSize > 1024 * 1024) {
                                "%.1f MB".format(fileSize / (1024.0 * 1024.0))
                            } else {
                                "%.1f KB".format(fileSize / 1024.0)
                            }

                            _currentDownloadState.value = DownloadState.Completed(
                                title = downloadedFile.nameWithoutExtension,
                                outputPath = "Galeria / Kitsune",
                                fileSizeFormatted = formattedSize
                            )

                            val completedNotif = DownloadNotificationHelper.buildCompletedNotification(
                                context = this@DownloadForegroundService,
                                title = downloadedFile.nameWithoutExtension,
                                subtext = getString(R.string.status_saved_gallery),
                                targetUri = exportResult.getOrNull(),
                                isAudioOnly = config.audioOnly
                            )
                            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                            manager.notify(DownloadNotificationHelper.NOTIFICATION_ID + 1, completedNotif)
                        },
                        onFailure = { err ->
                            _currentDownloadState.value = DownloadState.Error(err.localizedMessage ?: "Erro ao salvar na galeria.")
                        }
                    )
                },
                onFailure = { error ->
                    _currentDownloadState.value = DownloadState.Error(error.localizedMessage ?: "Falha ao processar download.")
                }
            )

            cleanupAndStop()
        }
    }

    private fun handleCancelDownload() {
        downloadJob?.cancel()
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
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
}
