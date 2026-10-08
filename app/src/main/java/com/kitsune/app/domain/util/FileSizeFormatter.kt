package com.kitsune.app.domain.util

import java.util.Locale

object FileSizeFormatter {

    private const val KB = 1024.0
    private const val MB = KB * 1024
    private const val GB = MB * 1024

    fun format(bytes: Long): String = when {
        bytes >= GB -> String.format(Locale.US, "%.1f GB", bytes / GB)
        bytes >= MB -> String.format(Locale.US, "%.1f MB", bytes / MB)
        bytes >= KB -> String.format(Locale.US, "%.1f KB", bytes / KB)
        else -> "$bytes B"
    }
}
