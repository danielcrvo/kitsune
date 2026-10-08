package com.kitsune.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FileSizeFormatterTest {

    @Test
    fun formatsBytesWithBinaryUnits() {
        assertEquals("500 B", FileSizeFormatter.format(500L))
        assertEquals("1.0 KB", FileSizeFormatter.format(1024L))
        assertEquals("1.5 KB", FileSizeFormatter.format(1536L))
        assertEquals("10.0 MB", FileSizeFormatter.format(10 * 1024 * 1024L))
        assertEquals("1.2 GB", FileSizeFormatter.format((1.2 * 1024 * 1024 * 1024).toLong()))
    }
}
