package com.kitsune.app.core.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import java.io.File

@Immutable
data class DownloadedMediaFile(
    val id: Long,
    val title: String,
    val fileName: String,
    val uri: Uri,
    val file: File?,
    val sizeBytes: Long,
    val sizeFormatted: String,
    val dateModified: Long,
    val isVideo: Boolean,
    val mimeType: String
)
