package com.kitsune.app.ui.screens

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kitsune.app.R
import com.kitsune.app.core.engine.AppUpdateManager
import com.kitsune.app.core.engine.EngineUpdateManager
import com.kitsune.app.core.engine.UrlDetector
import com.kitsune.app.core.engine.YtDlpEngine
import com.kitsune.app.core.model.AppUpdateInfo
import com.kitsune.app.core.model.AppUpdateState
import com.kitsune.app.core.model.AudioCodec
import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.DownloadState
import com.kitsune.app.core.model.DownloadedMediaFile
import com.kitsune.app.core.model.PlaylistInfo
import com.kitsune.app.core.service.DownloadForegroundService
import com.kitsune.app.core.storage.DownloadedFilesRepository
import com.kitsune.app.core.storage.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var metadataJob: Job? = null

    init {
        viewModelScope.launch {
            DownloadForegroundService.currentDownloadState.collect { state ->
                _uiState.update { it.copy(downloadState = state) }
                if (state is DownloadState.Completed) {
                    loadDownloadedFiles()
                }
            }
        }

        viewModelScope.launch {
            DownloadForegroundService.downloadQueue.collect { queue ->
                _uiState.update { it.copy(downloadQueue = queue) }
            }
        }

        viewModelScope.launch {
            val savedConfig = preferencesRepository.userConfigFlow.firstOrNull()
            val savedMode = preferencesRepository.downloadModeFlow.firstOrNull() ?: DownloadMode.AUTO

            if (savedConfig != null) {
                val configWithMode = when (savedMode) {
                    DownloadMode.AUTO -> savedConfig.copy(audioOnly = false, muteAudio = false)
                    DownloadMode.AUDIO -> savedConfig.copy(audioOnly = true, muteAudio = false, audioCodec = AudioCodec.MP3)
                    DownloadMode.MUTE -> savedConfig.copy(audioOnly = false, muteAudio = true)
                }
                _uiState.update { it.copy(downloadConfig = configWithMode) }
            }
        }

        viewModelScope.launch {
            preferencesRepository.isAmoledThemeFlow.collect { amoled ->
                _uiState.update { it.copy(isAmoledTheme = amoled) }
            }
        }

        viewModelScope.launch {
            preferencesRepository.isDynamicColorFlow.collect { dynamicColor ->
                _uiState.update { it.copy(isDynamicColor = dynamicColor) }
            }
        }

        viewModelScope.launch {
            preferencesRepository.isWifiOnlyFlow.collect { wifiOnly ->
                _uiState.update { it.copy(isWifiOnly = wifiOnly) }
            }
        }

        viewModelScope.launch {
            preferencesRepository.isAutoCheckUpdatesFlow.collect { autoCheck ->
                _uiState.update { it.copy(isAutoCheckUpdates = autoCheck) }
            }
        }

        loadDownloadedFiles()
        refreshApkCacheSize()

        val currentAppVersion = AppUpdateManager.getCurrentVersionName(application)
        _uiState.update { it.copy(appVersionName = currentAppVersion) }

        viewModelScope.launch {
            checkAppUpdateSilently()
        }
    }

    fun onAction(action: MainUiAction, context: Context? = null) {
        when (action) {
            is MainUiAction.ChangeUrl -> onUrlChanged(action.url)
            is MainUiAction.StartDownload -> context?.let { startDownload(it) }
            is MainUiAction.CancelDownload -> context?.let { cancelDownload(it) }
            is MainUiAction.DismissError -> dismissError()
            is MainUiAction.SetDownloadMode -> setDownloadMode(action.mode)
            is MainUiAction.ChangeConfig -> onConfigChanged(action.config)
            is MainUiAction.ToggleSettings -> toggleSettingsSheet(action.open)
            is MainUiAction.ToggleHistory -> toggleHistory(action.open)
            is MainUiAction.ToggleSupportedServices -> toggleSupportedServices(action.open)
            is MainUiAction.ToggleTerms -> toggleTerms(action.open)
            is MainUiAction.ToggleAmoledTheme -> toggleAmoledTheme(action.enabled)
            is MainUiAction.ToggleDynamicColor -> toggleDynamicColor(action.enabled)
            is MainUiAction.ToggleWifiOnly -> toggleWifiOnly(action.enabled)
            is MainUiAction.CancelQueueTask -> context?.let { cancelQueueTask(it, action.taskId) }
            is MainUiAction.OpenPlaylistDialog -> openPlaylistDialog(action.info)
            is MainUiAction.ClosePlaylistDialog -> closePlaylistDialog()
            is MainUiAction.TogglePlaylistItem -> togglePlaylistItem(action.itemId)
            is MainUiAction.SelectAllPlaylistItems -> selectAllPlaylistItems()
            is MainUiAction.DeselectAllPlaylistItems -> deselectAllPlaylistItems()
            is MainUiAction.DownloadSelectedPlaylistItems -> context?.let { downloadSelectedPlaylistItems(it) }
            is MainUiAction.CheckEngineUpdate -> context?.let { checkEngineUpdate(it) }
            is MainUiAction.PlayFile -> playDownloadedFile(action.file)
            is MainUiAction.CloseMediaPlayer -> closeMediaPlayer()
            is MainUiAction.PlayExternal -> context?.let { playFileExternal(it, action.file) }
            is MainUiAction.DeleteFile -> confirmDeleteFile(action.file)
            is MainUiAction.RenameFile -> confirmRenameFile(action.file, action.newName)
            is MainUiAction.CheckAppUpdate -> checkAppUpdateManually()
            is MainUiAction.DownloadAppUpdate -> downloadAppUpdate()
            is MainUiAction.InstallAppUpdate -> context?.let { installAppUpdate(it) }
            is MainUiAction.DismissAppUpdateDialog -> dismissAppUpdateDialog()
            is MainUiAction.ToggleAbout -> toggleAbout(action.open)
            is MainUiAction.ToggleAutoCheckUpdates -> toggleAutoCheckUpdates(action.enabled)
            is MainUiAction.ClearUpdateCache -> clearUpdateCache()
        }
    }

    fun initEngineVersion(context: Context) {
        viewModelScope.launch {
            try {
                val version = EngineUpdateManager.getEngineVersion(context)
                _uiState.update { it.copy(engineVersion = version) }
            } catch (t: Throwable) {
                _uiState.update { it.copy(engineVersion = "N/A") }
            }
        }
    }

    fun checkClipboardForMediaUrl(context: Context) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()

            if (!clipText.isNullOrBlank() && UrlDetector.isValidUrl(clipText)) {
                val currentUrl = _uiState.value.url.trim()
                if (currentUrl.isBlank()) {
                    onUrlChanged(clipText)
                } else if (currentUrl != clipText) {
                    _uiState.update { it.copy(detectedClipboardUrl = clipText) }
                }
            }
        } catch (_: Throwable) {}
    }

    fun onUrlChanged(newUrl: String) {
        metadataJob?.cancel()
        val detected = UrlDetector.detect(newUrl)
        val isValid = UrlDetector.isValidUrl(newUrl)
        val isPlaylist = UrlDetector.isPlaylistUrl(newUrl)

        _uiState.update {
            it.copy(
                url = newUrl,
                detectedPlatform = detected,
                isUrlValid = isValid,
                mediaInfo = if (newUrl.isBlank()) null else it.mediaInfo,
                isLoadingMetadata = isValid && it.mediaInfo == null && !isPlaylist,
                isLoadingPlaylist = isValid && isPlaylist,
                detectedClipboardUrl = null
            )
        }

        if (isValid) {
            metadataJob = viewModelScope.launch {
                if (isPlaylist) {
                    try {
                        _uiState.update { it.copy(isLoadingPlaylist = true) }
                        val result = YtDlpEngine.fetchPlaylistInfo(getApplication(), newUrl)
                        result.fold(
                            onSuccess = { playlist ->
                                val allIds = playlist.items.map { item -> item.id }.toSet()
                                _uiState.update {
                                    it.copy(
                                        playlistInfo = playlist,
                                        selectedPlaylistItems = allIds,
                                        isPlaylistDialogOpen = true,
                                        isLoadingPlaylist = false
                                    )
                                }
                            },
                            onFailure = {
                                _uiState.update { it.copy(isLoadingPlaylist = false) }
                            }
                        )
                    } catch (_: Throwable) {
                        _uiState.update { it.copy(isLoadingPlaylist = false) }
                    }
                } else {
                    try {
                        _uiState.update { it.copy(isLoadingMetadata = true) }
                        val result = YtDlpEngine.fetchMediaInfo(getApplication(), newUrl)
                        result.fold(
                            onSuccess = { info ->
                                _uiState.update {
                                    it.copy(
                                        mediaInfo = info,
                                        isLoadingMetadata = false
                                    )
                                }
                            },
                            onFailure = {
                                _uiState.update { it.copy(isLoadingMetadata = false) }
                            }
                        )
                    } catch (_: Throwable) {
                        _uiState.update { it.copy(isLoadingMetadata = false) }
                    }
                }
            }
        }
    }

    fun onConfigChanged(newConfig: DownloadConfig) {
        _uiState.update { it.copy(downloadConfig = newConfig) }
        viewModelScope.launch {
            preferencesRepository.saveDownloadConfig(newConfig)
        }
    }

    fun setDownloadMode(mode: DownloadMode) {
        _uiState.update { state ->
            val newConfig = when (mode) {
                DownloadMode.AUTO -> state.downloadConfig.copy(audioOnly = false, muteAudio = false)
                DownloadMode.AUDIO -> state.downloadConfig.copy(audioOnly = true, muteAudio = false, audioCodec = AudioCodec.MP3)
                DownloadMode.MUTE -> state.downloadConfig.copy(audioOnly = false, muteAudio = true)
            }
            state.copy(downloadConfig = newConfig)
        }
        viewModelScope.launch {
            preferencesRepository.saveDownloadMode(mode)
        }
    }

    fun toggleSettingsSheet(open: Boolean) {
        _uiState.update { it.copy(isSettingsSheetOpen = open) }
    }

    fun toggleSupportedServices(open: Boolean) {
        _uiState.update { it.copy(isSupportedServicesOpen = open) }
    }

    fun toggleTerms(open: Boolean) {
        _uiState.update { it.copy(isTermsOpen = open) }
    }

    fun toggleHistory(open: Boolean) {
        _uiState.update { it.copy(isHistoryOpen = open) }
        if (open) {
            loadDownloadedFiles()
        }
    }

    fun loadDownloadedFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDownloadedFiles = true) }
            val files = DownloadedFilesRepository.getDownloadedFiles(getApplication())
            _uiState.update {
                it.copy(
                    downloadedFiles = files,
                    isLoadingDownloadedFiles = false
                )
            }
        }
    }

    fun playDownloadedFile(item: DownloadedMediaFile) {
        _uiState.update { it.copy(playingFile = item) }
    }

    fun closeMediaPlayer() {
        _uiState.update { it.copy(playingFile = null) }
    }

    fun playFileExternal(context: Context, item: DownloadedMediaFile) {
        DownloadedFilesRepository.playFile(context, item)
    }

    fun setRenamingFile(item: DownloadedMediaFile?) {
        _uiState.update { it.copy(renamingFile = item) }
    }

    fun setDeletingFile(item: DownloadedMediaFile?) {
        _uiState.update { it.copy(deletingFile = item) }
    }

    fun confirmDeleteFile(item: DownloadedMediaFile) {
        viewModelScope.launch {
            val success = DownloadedFilesRepository.deleteFile(getApplication(), item)
            _uiState.update { state ->
                state.copy(
                    deletingFile = null,
                    downloadedFiles = state.downloadedFiles.filterNot { it.id == item.id || it.uri == item.uri },
                    toastMessage = if (success) {
                        getApplication<Application>().getString(R.string.history_file_deleted)
                    } else {
                        getApplication<Application>().getString(R.string.history_file_delete_failed)
                    }
                )
            }
        }
    }

    fun confirmRenameFile(item: DownloadedMediaFile, newNameWithoutExt: String) {
        viewModelScope.launch {
            val result = DownloadedFilesRepository.renameFile(getApplication(), item, newNameWithoutExt)
            result.fold(
                onSuccess = { updatedItem ->
                    _uiState.update { state ->
                        state.copy(
                            renamingFile = null,
                            downloadedFiles = state.downloadedFiles.map { if (it.id == item.id) updatedItem else it },
                            toastMessage = getApplication<Application>().getString(R.string.history_file_renamed)
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            renamingFile = null,
                            toastMessage = getApplication<Application>().getString(
                                R.string.history_file_rename_failed,
                                err.localizedMessage ?: ""
                            )
                        )
                    }
                }
            )
        }
    }

    fun startDownload(context: Context) {
        val currentState = _uiState.value
        if (!currentState.isUrlValid) return

        DownloadForegroundService.startDownload(
            context = context,
            url = currentState.url,
            config = currentState.downloadConfig
        )
    }

    fun cancelDownload(context: Context) {
        DownloadForegroundService.cancelDownload(context)
    }

    fun dismissError() {
        _uiState.update { it.copy(downloadState = DownloadState.Idle) }
    }

    fun checkEngineUpdate(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUpdate = true) }
            val status = EngineUpdateManager.checkForUpdates(context)
            val newVersion = EngineUpdateManager.getEngineVersion(context)
            _uiState.update {
                it.copy(
                    isCheckingUpdate = false,
                    engineVersion = newVersion,
                    toastMessage = when (status) {
                        is EngineUpdateManager.UpdateStatus.Updated -> getApplication<Application>().getString(
                            R.string.settings_engine_updated_to,
                            status.version
                        )
                        is EngineUpdateManager.UpdateStatus.AlreadyUpToDate -> getApplication<Application>().getString(
                            R.string.settings_engine_updated
                        )
                        is EngineUpdateManager.UpdateStatus.Error -> getApplication<Application>().getString(
                            R.string.settings_engine_update_failed,
                            status.error
                        )
                        else -> null
                    }
                )
            }
        }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun toggleAmoledTheme(enabled: Boolean) {
        _uiState.update { it.copy(isAmoledTheme = enabled) }
        viewModelScope.launch {
            preferencesRepository.saveAmoledTheme(enabled)
        }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        _uiState.update { it.copy(isDynamicColor = enabled) }
        viewModelScope.launch {
            preferencesRepository.saveDynamicColor(enabled)
        }
    }

    fun toggleWifiOnly(enabled: Boolean) {
        _uiState.update { it.copy(isWifiOnly = enabled) }
        viewModelScope.launch {
            preferencesRepository.saveWifiOnly(enabled)
        }
    }

    fun cancelQueueTask(context: Context, taskId: String) {
        DownloadForegroundService.cancelTask(context, taskId)
    }

    fun openPlaylistDialog(info: PlaylistInfo? = null) {
        if (info != null) {
            _uiState.update {
                it.copy(
                    playlistInfo = info,
                    selectedPlaylistItems = info.items.map { item -> item.id }.toSet(),
                    isPlaylistDialogOpen = true
                )
            }
        } else if (_uiState.value.playlistInfo != null) {
            _uiState.update { it.copy(isPlaylistDialogOpen = true) }
        }
    }

    fun closePlaylistDialog() {
        _uiState.update { it.copy(isPlaylistDialogOpen = false) }
    }

    fun togglePlaylistItem(itemId: String) {
        _uiState.update { state ->
            val current = state.selectedPlaylistItems
            val updated = if (current.contains(itemId)) {
                current - itemId
            } else {
                current + itemId
            }
            state.copy(selectedPlaylistItems = updated)
        }
    }

    fun selectAllPlaylistItems() {
        _uiState.update { state ->
            val allIds = state.playlistInfo?.items?.map { it.id }?.toSet() ?: emptySet()
            state.copy(selectedPlaylistItems = allIds)
        }
    }

    fun deselectAllPlaylistItems() {
        _uiState.update { it.copy(selectedPlaylistItems = emptySet()) }
    }

    fun downloadSelectedPlaylistItems(context: Context) {
        val state = _uiState.value
        val playlist = state.playlistInfo ?: return
        val selected = playlist.items.filter { state.selectedPlaylistItems.contains(it.id) }
        if (selected.isEmpty()) return

        val urls = ArrayList(selected.map { it.url })
        val titles = ArrayList(selected.map { it.title })

        DownloadForegroundService.enqueueBatch(
            context = context,
            urls = urls,
            titles = titles,
            config = state.downloadConfig
        )

        _uiState.update {
            it.copy(
                isPlaylistDialogOpen = false,
                url = "",
                isUrlValid = false,
                playlistInfo = null,
                selectedPlaylistItems = emptySet()
            )
        }
    }

    private suspend fun checkAppUpdateSilently() {
        if (!preferencesRepository.isAutoCheckUpdatesFlow.firstOrNull().let { it ?: true }) {
            return
        }
        try {
            val result = AppUpdateManager.checkForAppUpdate(getApplication())
            result.fold(
                onSuccess = { info ->
                    if (info != null) {
                        _uiState.update {
                            it.copy(
                                appUpdateInfo = info,
                                appUpdateState = AppUpdateState.UpdateAvailable(info),
                                isAppUpdateDialogOpen = true
                            )
                        }
                        AppUpdateManager.notifyUpdateAvailable(getApplication(), info.versionName)
                    }
                },
                onFailure = {}
            )
        } catch (_: Throwable) {}
    }

    fun checkAppUpdateManually() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAppUpdate = true) }
            val result = AppUpdateManager.checkForAppUpdate(getApplication())
            _uiState.update { it.copy(isCheckingAppUpdate = false) }
            result.fold(
                onSuccess = { info ->
                    if (info != null) {
                        _uiState.update {
                            it.copy(
                                appUpdateInfo = info,
                                appUpdateState = AppUpdateState.UpdateAvailable(info),
                                isAppUpdateDialogOpen = true
                            )
                        }
                        AppUpdateManager.notifyUpdateAvailable(getApplication(), info.versionName)
                    } else {
                        _uiState.update {
                            it.copy(
                                toastMessage = getApplication<Application>().getString(R.string.settings_app_up_to_date)
                            )
                        }
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            toastMessage = getApplication<Application>().getString(
                                R.string.update_download_failed,
                                err.localizedMessage ?: ""
                            )
                        )
                    }
                }
            )
        }
    }

    fun downloadAppUpdate() {
        val info = _uiState.value.appUpdateInfo ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    appUpdateState = AppUpdateState.Downloading(
                        info = info,
                        progressPercent = 0,
                        bytesDownloaded = 0L,
                        totalBytes = info.fileSizeBytes
                    )
                )
            }
            val result = AppUpdateManager.downloadUpdateApk(getApplication(), info) { percent, read, total ->
                _uiState.update {
                    it.copy(
                        appUpdateState = AppUpdateState.Downloading(
                            info = info,
                            progressPercent = percent,
                            bytesDownloaded = read,
                            totalBytes = total
                        )
                    )
                }
            }
            result.fold(
                onSuccess = { apkFile ->
                    refreshApkCacheSize()
                    _uiState.update {
                        it.copy(
                            appUpdateState = AppUpdateState.ReadyToInstall(
                                info = info,
                                apkPath = apkFile.absolutePath
                            )
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            appUpdateState = AppUpdateState.Error(
                                err.localizedMessage ?: "Download failed"
                            )
                        )
                    }
                }
            )
        }
    }

    fun installAppUpdate(context: Context) {
        val state = _uiState.value.appUpdateState
        if (state is AppUpdateState.ReadyToInstall) {
            val apkFile = File(state.apkPath)
            val launched = AppUpdateManager.installApk(context, apkFile)
            if (!launched) {
                _uiState.update {
                    it.copy(
                        toastMessage = getApplication<Application>().getString(R.string.update_permission_required)
                    )
                }
            }
        }
    }

    fun dismissAppUpdateDialog() {
        _uiState.update { it.copy(isAppUpdateDialogOpen = false) }
    }

    fun toggleAbout(open: Boolean) {
        _uiState.update { it.copy(isAboutDialogOpen = open) }
    }

    fun toggleAutoCheckUpdates(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveAutoCheckUpdates(enabled)
        }
    }

    fun refreshApkCacheSize() {
        viewModelScope.launch {
            val size = AppUpdateManager.getUpdateCacheSizeBytes(getApplication())
            _uiState.update { it.copy(apkCacheSizeBytes = size) }
        }
    }

    fun clearUpdateCache() {
        viewModelScope.launch {
            val freedBytes = AppUpdateManager.clearUpdateCache(getApplication())
            refreshApkCacheSize()
            val msg = if (freedBytes > 0) {
                getApplication<Application>().getString(
                    R.string.settings_clear_cache_success,
                    AppUpdateManager.formatFileSize(freedBytes)
                )
            } else {
                getApplication<Application>().getString(R.string.settings_clear_cache_empty)
            }
            _uiState.update {
                it.copy(toastMessage = msg)
            }
        }
    }
}
