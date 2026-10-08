package com.kitsune.app.data.update

import java.io.File
import java.security.MessageDigest
import java.util.Locale

object AppUpdateUtils {

    data class ReleaseAsset(
        val name: String,
        val downloadUrl: String,
        val size: Long,
        val sha256: String? = null
    )

    fun isVersionNewer(latestVersion: String, currentVersion: String): Boolean {
        val latestParts = numericParts(latestVersion)
        val currentParts = numericParts(currentVersion)

        val maxLength = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLength) {
            val latest = latestParts.getOrElse(i) { 0 }
            val current = currentParts.getOrElse(i) { 0 }
            if (latest > current) return true
            if (latest < current) return false
        }
        return false
    }

    private fun numericParts(version: String): List<Int> = version.trim()
        .removePrefix("v")
        .removePrefix("V")
        .substringBefore("-")
        .split(".")
        .mapNotNull { it.toIntOrNull() }

    fun selectBestApkAsset(assets: List<ReleaseAsset>, supportedAbis: List<String>): ReleaseAsset? {
        val apkAssets = assets.filter {
            it.name.endsWith(".apk", ignoreCase = true) && !it.name.contains("fdroid", ignoreCase = true)
        }
        if (apkAssets.isEmpty()) return null

        for (abi in supportedAbis) {
            apkAssets.firstOrNull { it.name.contains(abi, ignoreCase = true) }?.let { return it }
        }

        return apkAssets.firstOrNull { it.name.contains("universal", ignoreCase = true) }
            ?: apkAssets.first()
    }

    fun parseSha256Digest(digest: String?): String? {
        val value = digest?.trim().orEmpty()
        if (!value.startsWith("sha256:", ignoreCase = true)) return null
        val hex = value.substringAfter(':').lowercase(Locale.US)
        return hex.takeIf { it.length == 64 && it.all { c -> c in '0'..'9' || c in 'a'..'f' } }
    }

    fun sha256Hex(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
