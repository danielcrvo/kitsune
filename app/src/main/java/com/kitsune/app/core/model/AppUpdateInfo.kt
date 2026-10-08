package com.kitsune.app.core.model

data class AppUpdateInfo(
    val versionName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val apkFileName: String,
    val fileSizeBytes: Long,
    val sha256: String? = null
)
