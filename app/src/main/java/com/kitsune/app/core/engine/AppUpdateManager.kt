package com.kitsune.app.core.engine

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.kitsune.app.R
import com.kitsune.app.core.model.AppUpdateInfo
import com.kitsune.app.core.service.DownloadNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Locale

object AppUpdateManager {

    private const val GITHUB_LATEST_RELEASE_URL = "https://api.github.com/repos/danielcrvo/kitsune/releases/latest"

    data class ReleaseAsset(
        val name: String,
        val downloadUrl: String,
        val size: Long,
        val sha256: String? = null
    )

    fun getCurrentVersionName(context: Context): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).versionName ?: "1.0.0"
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
            }
        } catch (_: Throwable) {
            "1.0.0"
        }
    }

    fun isVersionNewer(latestVersion: String, currentVersion: String): Boolean {
        val vClean1 = latestVersion.trim().removePrefix("v").removePrefix("V").substringBefore("-")
        val vClean2 = currentVersion.trim().removePrefix("v").removePrefix("V").substringBefore("-")

        val parts1 = vClean1.split(".").mapNotNull { it.toIntOrNull() }
        val parts2 = vClean2.split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLength) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 > p2) return true
            if (p1 < p2) return false
        }
        return false
    }

    fun selectBestApkAsset(assets: List<ReleaseAsset>): ReleaseAsset? {
        val apkAssets = assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
        if (apkAssets.isEmpty()) return null

        val supportedAbis = Build.SUPPORTED_ABIS ?: emptyArray()
        for (abi in supportedAbis) {
            val matched = apkAssets.firstOrNull { it.name.contains(abi, ignoreCase = true) }
            if (matched != null) return matched
        }

        val universal = apkAssets.firstOrNull { it.name.contains("universal", ignoreCase = true) }
        if (universal != null) return universal

        return apkAssets.firstOrNull()
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

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f GB", bytes.toDouble() / (1024 * 1024 * 1024))
            bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes.toDouble() / 1024)
            else -> "$bytes B"
        }
    }

    suspend fun checkForAppUpdate(context: Context): Result<AppUpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val currentVersion = getCurrentVersionName(context)
            val url = URL(GITHUB_LATEST_RELEASE_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "Kitsune-App/$currentVersion")
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                return@withContext Result.failure(Exception("HTTP error $responseCode"))
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val json = JSONObject(responseText)
            val tagName = json.optString("tag_name", "")
            val releaseTitle = json.optString("name", tagName)
            val releaseNotes = json.optString("body", "")

            if (!isVersionNewer(tagName, currentVersion)) {
                return@withContext Result.success(null)
            }

            val assetsJson = json.optJSONArray("assets") ?: JSONArray()
            val parsedAssets = mutableListOf<ReleaseAsset>()
            for (i in 0 until assetsJson.length()) {
                val assetObj = assetsJson.getJSONObject(i)
                parsedAssets.add(
                    ReleaseAsset(
                        name = assetObj.optString("name", ""),
                        downloadUrl = assetObj.optString("browser_download_url", ""),
                        size = assetObj.optLong("size", 0L),
                        sha256 = parseSha256Digest(assetObj.optString("digest", ""))
                    )
                )
            }

            val bestAsset = selectBestApkAsset(parsedAssets)
                ?: return@withContext Result.failure(Exception("No compatible APK asset found in release"))

            val updateInfo = AppUpdateInfo(
                versionName = tagName.removePrefix("v").removePrefix("V"),
                releaseTitle = releaseTitle,
                releaseNotes = releaseNotes,
                downloadUrl = bestAsset.downloadUrl,
                apkFileName = bestAsset.name,
                fileSizeBytes = bestAsset.size,
                sha256 = bestAsset.sha256
            )

            Result.success(updateInfo)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun downloadUpdateApk(
        context: Context,
        updateInfo: AppUpdateInfo,
        onProgress: (progressPercent: Int, bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        var currentUrl = updateInfo.downloadUrl
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            val updateDir = File(context.cacheDir, "apk_updates")
            if (!updateDir.exists()) {
                updateDir.mkdirs()
            }
            val safeFileName = File(updateInfo.apkFileName).name.ifBlank { "kitsune-update.apk" }
            val destinationFile = File(updateDir, safeFileName)
            val partialFile = File(updateDir, "$safeFileName.part")
            destinationFile.delete()
            partialFile.delete()

            var redirectCount = 0
            while (redirectCount < 5) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Kitsune-App/${getCurrentVersionName(context)}")
                }

                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                    code == HttpURLConnection.HTTP_MOVED_TEMP ||
                    code == 307 || code == 308
                ) {
                    val location = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (location != null) {
                        currentUrl = location
                        redirectCount++
                        continue
                    }
                }
                break
            }

            val finalConn = connection ?: return@withContext Result.failure(Exception("Connection failed"))
            val responseCode = finalConn.responseCode
            if (responseCode !in 200..299) {
                finalConn.disconnect()
                return@withContext Result.failure(Exception("HTTP error $responseCode"))
            }

            val contentLength = finalConn.contentLengthLong.let { if (it > 0) it else updateInfo.fileSizeBytes }
            inputStream = finalConn.inputStream
            outputStream = FileOutputStream(partialFile)

            val buffer = ByteArray(16384)
            var bytesRead: Int
            var totalBytesRead = 0L
            var lastReportedPercent = -1

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                val percent = if (contentLength > 0) {
                    ((totalBytesRead * 100) / contentLength).toInt().coerceIn(0, 100)
                } else 0

                if (percent != lastReportedPercent) {
                    lastReportedPercent = percent
                    onProgress(percent, totalBytesRead, contentLength)

                    val statusText = context.getString(
                        R.string.update_notif_downloading_desc,
                        formatFileSize(totalBytesRead),
                        formatFileSize(contentLength),
                        percent
                    )
                    val notif = DownloadNotificationHelper.buildAppUpdateProgressNotification(
                        context = context,
                        versionName = updateInfo.versionName,
                        progressPercent = percent,
                        statusText = statusText
                    )
                    notificationManager?.notify(DownloadNotificationHelper.UPDATE_NOTIFICATION_ID, notif)
                }
            }

            outputStream.flush()
            outputStream.close()
            outputStream = null

            val expectedSha256 = updateInfo.sha256
            if (expectedSha256 != null) {
                val actualSha256 = sha256Hex(partialFile)
                if (!actualSha256.equals(expectedSha256, ignoreCase = true)) {
                    partialFile.delete()
                    throw SecurityException("Downloaded APK checksum does not match the published SHA-256 digest.")
                }
            }

            if (!partialFile.renameTo(destinationFile)) {
                partialFile.delete()
                throw IllegalStateException("Could not finalize downloaded APK.")
            }

            val readyNotif = DownloadNotificationHelper.buildAppUpdateReadyNotification(
                context = context,
                versionName = updateInfo.versionName,
                apkFile = destinationFile
            )
            notificationManager?.notify(DownloadNotificationHelper.UPDATE_NOTIFICATION_ID, readyNotif)

            Result.success(destinationFile)
        } catch (t: Throwable) {
            notificationManager?.cancel(DownloadNotificationHelper.UPDATE_NOTIFICATION_ID)
            runCatching {
                File(File(context.cacheDir, "apk_updates"), "${File(updateInfo.apkFileName).name}.part").delete()
            }
            Result.failure(t)
        } finally {
            try { outputStream?.close() } catch (_: Throwable) {}
            try { inputStream?.close() } catch (_: Throwable) {}
            try { connection?.disconnect() } catch (_: Throwable) {}
        }
    }

    fun notifyUpdateAvailable(context: Context, versionName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val notif = DownloadNotificationHelper.buildAppUpdateAvailableNotification(context, versionName)
        notificationManager?.notify(DownloadNotificationHelper.UPDATE_NOTIFICATION_ID, notif)
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists()) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                return false
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(installIntent)
        return true
    }

    fun getUpdateCacheSizeBytes(context: Context): Long {
        return try {
            val updateDir = File(context.cacheDir, "apk_updates")
            if (updateDir.exists() && updateDir.isDirectory) {
                updateDir.listFiles()?.sumOf { it.length() } ?: 0L
            } else 0L
        } catch (_: Throwable) {
            0L
        }
    }

    fun clearUpdateCache(context: Context): Long {
        return try {
            val updateDir = File(context.cacheDir, "apk_updates")
            var freedBytes = 0L
            if (updateDir.exists() && updateDir.isDirectory) {
                updateDir.listFiles()?.forEach { file ->
                    freedBytes += file.length()
                    file.delete()
                }
            }
            freedBytes
        } catch (_: Throwable) {
            0L
        }
    }
}
