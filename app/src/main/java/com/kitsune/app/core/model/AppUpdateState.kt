package com.kitsune.app.core.model

sealed interface AppUpdateState {
    data object Idle : AppUpdateState
    data object Checking : AppUpdateState
    data class UpdateAvailable(val info: AppUpdateInfo) : AppUpdateState
    data object UpToDate : AppUpdateState
    data class Downloading(
        val info: AppUpdateInfo,
        val progressPercent: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : AppUpdateState
    data class ReadyToInstall(
        val info: AppUpdateInfo,
        val apkPath: String
    ) : AppUpdateState
    data class Error(val message: String) : AppUpdateState
}
