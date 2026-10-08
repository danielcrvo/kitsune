package com.kitsune.app.core.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun `compara versoes semanticas corretamente`() {
        assertTrue(AppUpdateManager.isVersionNewer("v1.1.2", "1.1.1"))
        assertTrue(AppUpdateManager.isVersionNewer("1.2.0", "1.1.1"))
        assertTrue(AppUpdateManager.isVersionNewer("2.0.0", "1.9.9"))
        assertTrue(AppUpdateManager.isVersionNewer("v1.1.1.1", "1.1.1"))

        assertFalse(AppUpdateManager.isVersionNewer("v1.1.1", "1.1.1"))
        assertFalse(AppUpdateManager.isVersionNewer("1.1.0", "1.1.1"))
        assertFalse(AppUpdateManager.isVersionNewer("v1.0.9", "1.1.1"))
        assertFalse(AppUpdateManager.isVersionNewer("1.1.1-nightly", "1.1.1"))
    }

    @Test
    fun `formata tamanho de arquivo em bytes corretamente`() {
        assertEquals("500 B", AppUpdateManager.formatFileSize(500L))
        assertEquals("1.0 KB", AppUpdateManager.formatFileSize(1024L))
        assertEquals("1.5 KB", AppUpdateManager.formatFileSize(1536L))
        assertEquals("10.0 MB", AppUpdateManager.formatFileSize(10 * 1024 * 1024L))
        assertEquals("1.2 GB", AppUpdateManager.formatFileSize((1.2 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun `seleciona asset universal quando nao ha abi compativel especifica`() {
        val assets = listOf(
            AppUpdateManager.ReleaseAsset(
                name = "Kitsune-v1.1.2-universal-release.apk",
                downloadUrl = "https://github.com/danielcrvo/kitsune/releases/download/v1.1.2/Kitsune-v1.1.2-universal-release.apk",
                size = 50000000L
            ),
            AppUpdateManager.ReleaseAsset(
                name = "checksums.txt",
                downloadUrl = "https://github.com/danielcrvo/kitsune/releases/download/v1.1.2/checksums.txt",
                size = 500L
            )
        )

        val selected = AppUpdateManager.selectBestApkAsset(assets)
        assertNotNull(selected)
        assertEquals("Kitsune-v1.1.2-universal-release.apk", selected?.name)
    }

    @Test
    fun `retorna nulo quando nao ha apks nos assets da release`() {
        val assets = listOf(
            AppUpdateManager.ReleaseAsset(
                name = "source-code.zip",
                downloadUrl = "https://example.com/source.zip",
                size = 1000L
            )
        )

        val selected = AppUpdateManager.selectBestApkAsset(assets)
        assertNull(selected)
    }

    @Test
    fun `interpreta digest sha256 publicado pelo github`() {
        val hex = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        assertEquals(hex, AppUpdateManager.parseSha256Digest("sha256:$hex"))
        assertEquals(hex, AppUpdateManager.parseSha256Digest("SHA256:${hex.uppercase()}"))
        assertNull(AppUpdateManager.parseSha256Digest(""))
        assertNull(AppUpdateManager.parseSha256Digest(null))
        assertNull(AppUpdateManager.parseSha256Digest("md5:abc"))
        assertNull(AppUpdateManager.parseSha256Digest("sha256:xyz"))
    }

    @Test
    fun `calcula sha256 de arquivos`() {
        val file = java.io.File.createTempFile("kitsune", ".bin")
        try {
            file.writeText("abc")
            assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                AppUpdateManager.sha256Hex(file)
            )
        } finally {
            file.delete()
        }
    }
}
