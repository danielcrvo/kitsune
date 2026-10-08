package com.kitsune.app.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateUtilsTest {

    @Test
    fun `compara versoes semanticas corretamente`() {
        assertTrue(AppUpdateUtils.isVersionNewer("v1.1.2", "1.1.1"))
        assertTrue(AppUpdateUtils.isVersionNewer("1.2.0", "1.1.1"))
        assertTrue(AppUpdateUtils.isVersionNewer("2.0.0", "1.9.9"))
        assertTrue(AppUpdateUtils.isVersionNewer("v1.1.1.1", "1.1.1"))

        assertFalse(AppUpdateUtils.isVersionNewer("v1.1.1", "1.1.1"))
        assertFalse(AppUpdateUtils.isVersionNewer("1.1.0", "1.1.1"))
        assertFalse(AppUpdateUtils.isVersionNewer("v1.0.9", "1.1.1"))
        assertFalse(AppUpdateUtils.isVersionNewer("1.1.1-nightly", "1.1.1"))
    }

    @Test
    fun `seleciona asset universal quando nao ha abi compativel especifica`() {
        val assets = listOf(
            AppUpdateUtils.ReleaseAsset(
                name = "Kitsune-v1.1.2-universal-release.apk",
                downloadUrl = "https://github.com/danielcrvo/kitsune/releases/download/v1.1.2/Kitsune-v1.1.2-universal-release.apk",
                size = 50000000L
            ),
            AppUpdateUtils.ReleaseAsset(
                name = "checksums.txt",
                downloadUrl = "https://github.com/danielcrvo/kitsune/releases/download/v1.1.2/checksums.txt",
                size = 500L
            )
        )

        val selected = AppUpdateUtils.selectBestApkAsset(assets, listOf("arm64-v8a"))
        assertNotNull(selected)
        assertEquals("Kitsune-v1.1.2-universal-release.apk", selected?.name)
    }

    @Test
    fun `retorna nulo quando nao ha apks nos assets da release`() {
        val assets = listOf(
            AppUpdateUtils.ReleaseAsset(
                name = "source-code.zip",
                downloadUrl = "https://example.com/source.zip",
                size = 1000L
            )
        )

        val selected = AppUpdateUtils.selectBestApkAsset(assets, listOf("arm64-v8a"))
        assertNull(selected)
    }

    @Test
    fun `interpreta digest sha256 publicado pelo github`() {
        val hex = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        assertEquals(hex, AppUpdateUtils.parseSha256Digest("sha256:$hex"))
        assertEquals(hex, AppUpdateUtils.parseSha256Digest("SHA256:${hex.uppercase()}"))
        assertNull(AppUpdateUtils.parseSha256Digest(""))
        assertNull(AppUpdateUtils.parseSha256Digest(null))
        assertNull(AppUpdateUtils.parseSha256Digest("md5:abc"))
        assertNull(AppUpdateUtils.parseSha256Digest("sha256:xyz"))
    }

    @Test
    fun `calcula sha256 de arquivos`() {
        val file = java.io.File.createTempFile("kitsune", ".bin")
        try {
            file.writeText("abc")
            assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                AppUpdateUtils.sha256Hex(file)
            )
        } finally {
            file.delete()
        }
    }

    @Test
    fun `prioriza o apk da abi do aparelho e ignora builds fdroid`() {
        val assets = listOf(
            AppUpdateUtils.ReleaseAsset("Kitsune-v1.3.0-fdroid-arm64-v8a-release.apk", "https://example.com/f", 1L),
            AppUpdateUtils.ReleaseAsset("Kitsune-v1.3.0-universal-release.apk", "https://example.com/u", 3L),
            AppUpdateUtils.ReleaseAsset("Kitsune-v1.3.0-arm64-v8a-release.apk", "https://example.com/a", 2L)
        )

        val selected = AppUpdateUtils.selectBestApkAsset(assets, listOf("arm64-v8a", "armeabi-v7a"))
        assertEquals("Kitsune-v1.3.0-arm64-v8a-release.apk", selected?.name)
    }
}
