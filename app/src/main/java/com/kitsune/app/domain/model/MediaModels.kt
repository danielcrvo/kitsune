package com.kitsune.app.domain.model

data class ExportedMedia(
    val contentUri: String,
    val displayName: String,
    val sizeBytes: Long
)

data class DownloadRequest(
    val url: String,
    val title: String = ""
)

data class LinkAnalysis(
    val url: String,
    val platform: PlatformType,
    val isValid: Boolean,
    val isPlaylist: Boolean
)
