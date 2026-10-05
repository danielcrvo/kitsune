package com.kitsune.app.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class PlaylistItem(
    val id: String,
    val title: String,
    val url: String,
    val durationSeconds: Long = 0L,
    val uploader: String? = null,
    val thumbnailUrl: String? = null,
    val index: Int = 0
)

@Immutable
data class PlaylistInfo(
    val id: String,
    val title: String,
    val author: String? = null,
    val originalUrl: String,
    val items: List<PlaylistItem> = emptyList()
)
