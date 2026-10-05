package com.kitsune.app.core.model

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class DownloadTask(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String,
    val config: DownloadConfig,
    val state: DownloadState = DownloadState.Idle,
    val progress: Float = 0f,
    val speed: String = "",
    val eta: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
