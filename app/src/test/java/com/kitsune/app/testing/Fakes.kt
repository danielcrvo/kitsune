package com.kitsune.app.testing

import com.kitsune.app.domain.model.AppUpdateInfo
import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.model.DownloadMode
import com.kitsune.app.domain.model.DownloadProgress
import com.kitsune.app.domain.model.EngineUpdateResult
import com.kitsune.app.domain.model.ExportedMedia
import com.kitsune.app.domain.model.MediaInfo
import com.kitsune.app.domain.model.PlaylistInfo
import com.kitsune.app.domain.repository.AppUpdateRepository
import com.kitsune.app.domain.repository.DownloadScheduler
import com.kitsune.app.domain.repository.MediaEngine
import com.kitsune.app.domain.repository.MediaExporter
import com.kitsune.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class FakePreferencesRepository(
    config: DownloadConfig = DownloadConfig(),
    mode: DownloadMode = DownloadMode.AUTO,
    autoCheckUpdates: Boolean = true
) : PreferencesRepository {
    val configFlow = MutableStateFlow(config)
    val modeFlow = MutableStateFlow(mode)
    val amoledFlow = MutableStateFlow(false)
    val dynamicColorFlow = MutableStateFlow(false)
    val wifiOnlyFlow = MutableStateFlow(false)
    val autoCheckFlow = MutableStateFlow(autoCheckUpdates)

    override val downloadConfig: Flow<DownloadConfig> = configFlow
    override val downloadMode: Flow<DownloadMode> = modeFlow
    override val isAmoledTheme: Flow<Boolean> = amoledFlow
    override val isDynamicColor: Flow<Boolean> = dynamicColorFlow
    override val isWifiOnly: Flow<Boolean> = wifiOnlyFlow
    override val isAutoCheckUpdates: Flow<Boolean> = autoCheckFlow

    override suspend fun saveDownloadConfig(config: DownloadConfig) {
        configFlow.value = config
    }

    override suspend fun saveDownloadMode(mode: DownloadMode) {
        modeFlow.value = mode
    }

    override suspend fun setAmoledTheme(enabled: Boolean) {
        amoledFlow.value = enabled
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        dynamicColorFlow.value = enabled
    }

    override suspend fun setWifiOnly(enabled: Boolean) {
        wifiOnlyFlow.value = enabled
    }

    override suspend fun setAutoCheckUpdates(enabled: Boolean) {
        autoCheckFlow.value = enabled
    }
}

class FakeDownloadScheduler : DownloadScheduler {
    var startCount = 0
    val cancelledTaskIds = mutableListOf<String>()
    var cancelAllCount = 0

    override fun startProcessing() {
        startCount++
    }

    override fun cancelTask(taskId: String) {
        cancelledTaskIds += taskId
    }

    override fun cancelAll() {
        cancelAllCount++
    }
}

class FakeMediaEngine : MediaEngine {
    var mediaInfoResult: Result<MediaInfo> = Result.failure(IllegalStateException("not configured"))
    var playlistResult: Result<PlaylistInfo> = Result.failure(IllegalStateException("not configured"))
    var downloadResult: Result<File> = Result.failure(IllegalStateException("not configured"))
    val fetchedUrls = mutableListOf<String>()
    var progressToEmit: List<DownloadProgress> = emptyList()

    override suspend fun warmUp(): Result<Unit> = Result.success(Unit)

    override suspend fun fetchMediaInfo(url: String): Result<MediaInfo> {
        fetchedUrls += url
        return mediaInfoResult
    }

    override suspend fun fetchPlaylistInfo(url: String): Result<PlaylistInfo> {
        fetchedUrls += url
        return playlistResult
    }

    override suspend fun download(
        url: String,
        config: DownloadConfig,
        outputDir: File,
        onProgress: (DownloadProgress) -> Unit
    ): Result<File> {
        progressToEmit.forEach(onProgress)
        return downloadResult
    }

    override suspend fun engineVersion(): String = "2026.01.01"

    override suspend fun updateEngine(): EngineUpdateResult = EngineUpdateResult.AlreadyUpToDate
}

class FakeMediaExporter(
    var result: (File, String, Boolean) -> Result<ExportedMedia>
) : MediaExporter {
    val exportedTitles = mutableListOf<String>()

    override suspend fun export(sourceFile: File, title: String, isAudioOnly: Boolean): Result<ExportedMedia> {
        exportedTitles += title
        return result(sourceFile, title, isAudioOnly)
    }
}

class FakeAppUpdateRepository(
    override val isUpdaterEnabled: Boolean = true,
    var checkResult: Result<AppUpdateInfo?> = Result.success(null)
) : AppUpdateRepository {
    var checkCount = 0
    val notifiedVersions = mutableListOf<String>()

    override fun currentVersionName(): String = "1.2.1"

    override suspend fun checkForUpdate(): Result<AppUpdateInfo?> {
        checkCount++
        return checkResult
    }

    override suspend fun downloadUpdate(
        info: AppUpdateInfo,
        onProgress: (progressPercent: Int, bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): Result<File> = Result.failure(UnsupportedOperationException())

    override fun notifyUpdateAvailable(info: AppUpdateInfo) {
        notifiedVersions += info.versionName
    }

    override fun install(apkFile: File): Boolean = false

    override suspend fun cacheSizeBytes(): Long = 0L

    override suspend fun clearCache(): Long = 0L
}
