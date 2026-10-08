package com.kitsune.app.domain.repository

import com.kitsune.app.domain.model.AppUpdateInfo
import java.io.File

interface AppUpdateRepository {
    val isUpdaterEnabled: Boolean

    fun currentVersionName(): String
    suspend fun checkForUpdate(): Result<AppUpdateInfo?>
    suspend fun downloadUpdate(
        info: AppUpdateInfo,
        onProgress: (progressPercent: Int, bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): Result<File>
    fun notifyUpdateAvailable(info: AppUpdateInfo)
    fun install(apkFile: File): Boolean
    suspend fun cacheSizeBytes(): Long
    suspend fun clearCache(): Long
}
