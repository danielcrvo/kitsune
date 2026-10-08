package com.kitsune.app.data.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.kitsune.app.BuildConfig
import com.kitsune.app.R
import com.kitsune.app.di.IoDispatcher
import com.kitsune.app.domain.model.AppUpdateInfo
import com.kitsune.app.domain.repository.AppUpdateRepository
import com.kitsune.app.domain.util.FileSizeFormatter
import com.kitsune.app.service.KitsuneNotifier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubAppUpdateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val notifier: KitsuneNotifier
) : AppUpdateRepository {

    private companion object {
        const val LATEST_RELEASE_URL = "https://api.github.com/repos/danielcrvo/kitsune/releases/latest"
        const val UPDATE_DIR_NAME = "apk_updates"
        const val MAX_REDIRECTS = 5
        const val FALLBACK_VERSION = "1.0.0"
    }

    override val isUpdaterEnabled: Boolean = BuildConfig.UPDATER_ENABLED

    private val updateDir: File
        get() = File(context.cacheDir, UPDATE_DIR_NAME)

    override fun currentVersionName(): String = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                .versionName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }
    }.getOrNull() ?: FALLBACK_VERSION

    override suspend fun checkForUpdate(): Result<AppUpdateInfo?> = withContext(ioDispatcher) {
        runCatchingNonCancellable {
            val currentVersion = currentVersionName()
            val connection = openConnection(LATEST_RELEASE_URL, readTimeoutMs = 15_000).apply {
                setRequestProperty("Accept", "application/vnd.github.v3+json")
            }
            val responseText = try {
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw IllegalStateException("HTTP error $responseCode")
                }
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }

            val json = JSONObject(responseText)
            val tagName = json.optString("tag_name", "")
            if (!AppUpdateUtils.isVersionNewer(tagName, currentVersion)) {
                return@runCatchingNonCancellable null
            }

            val assetsJson = json.optJSONArray("assets") ?: JSONArray()
            val assets = (0 until assetsJson.length()).map { index ->
                val asset = assetsJson.getJSONObject(index)
                AppUpdateUtils.ReleaseAsset(
                    name = asset.optString("name", ""),
                    downloadUrl = asset.optString("browser_download_url", ""),
                    size = asset.optLong("size", 0L),
                    sha256 = AppUpdateUtils.parseSha256Digest(asset.optString("digest", ""))
                )
            }

            val bestAsset = AppUpdateUtils.selectBestApkAsset(assets, Build.SUPPORTED_ABIS.orEmpty().toList())
                ?: throw IllegalStateException("No compatible APK asset found in release")

            AppUpdateInfo(
                versionName = tagName.removePrefix("v").removePrefix("V"),
                releaseTitle = json.optString("name", tagName),
                releaseNotes = json.optString("body", ""),
                downloadUrl = bestAsset.downloadUrl,
                apkFileName = bestAsset.name,
                fileSizeBytes = bestAsset.size,
                sha256 = bestAsset.sha256
            )
        }
    }

    override suspend fun downloadUpdate(
        info: AppUpdateInfo,
        onProgress: (progressPercent: Int, bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(ioDispatcher) {
        val safeFileName = File(info.apkFileName).name.ifBlank { "kitsune-update.apk" }
        val destinationFile = File(updateDir, safeFileName)
        val partialFile = File(updateDir, "$safeFileName.part")

        val result = runCatchingNonCancellable {
            updateDir.mkdirs()
            destinationFile.delete()
            partialFile.delete()

            val connection = openFollowingRedirects(info.downloadUrl)
            try {
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw IllegalStateException("HTTP error $responseCode")
                }
                val contentLength = connection.contentLengthLong.takeIf { it > 0 } ?: info.fileSizeBytes

                connection.inputStream.use { input ->
                    FileOutputStream(partialFile).use { output ->
                        copyWithProgress(input, output, contentLength) { percent, bytesRead ->
                            onProgress(percent, bytesRead, contentLength)
                            notifier.showUpdateProgress(
                                versionName = info.versionName,
                                progressPercent = percent,
                                statusText = context.getString(
                                    R.string.update_notif_downloading_desc,
                                    FileSizeFormatter.format(bytesRead),
                                    FileSizeFormatter.format(contentLength),
                                    percent
                                )
                            )
                        }
                    }
                }
            } finally {
                connection.disconnect()
            }

            val expectedSha256 = info.sha256
            if (expectedSha256 != null &&
                !AppUpdateUtils.sha256Hex(partialFile).equals(expectedSha256, ignoreCase = true)
            ) {
                throw SecurityException("Downloaded APK checksum does not match the published SHA-256 digest.")
            }

            if (!partialFile.renameTo(destinationFile)) {
                throw IllegalStateException("Could not finalize downloaded APK.")
            }

            notifier.showUpdateReady(info.versionName, destinationFile)
            destinationFile
        }

        result.onFailure {
            partialFile.delete()
            notifier.cancelUpdateNotification()
        }
    }

    override fun notifyUpdateAvailable(info: AppUpdateInfo) {
        notifier.showUpdateAvailable(info.versionName)
    }

    override fun install(apkFile: File): Boolean {
        if (!apkFile.exists()) return false

        if (!context.packageManager.canRequestPackageInstalls()) {
            val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            runCatching { context.startActivity(settingsIntent) }
            return false
        }

        val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return runCatching { context.startActivity(installIntent) }.isSuccess
    }

    override suspend fun cacheSizeBytes(): Long = withContext(ioDispatcher) {
        updateDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    override suspend fun clearCache(): Long = withContext(ioDispatcher) {
        var freedBytes = 0L
        updateDir.listFiles()?.forEach { file ->
            val size = file.length()
            if (file.delete()) freedBytes += size
        }
        freedBytes
    }

    private fun openConnection(url: String, readTimeoutMs: Int): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = readTimeoutMs
            setRequestProperty("User-Agent", "Kitsune-App/${currentVersionName()}")
        }

    private fun openFollowingRedirects(initialUrl: String): HttpURLConnection {
        var currentUrl = initialUrl
        repeat(MAX_REDIRECTS) {
            val connection = openConnection(currentUrl, readTimeoutMs = 30_000).apply {
                instanceFollowRedirects = true
            }
            val code = connection.responseCode
            val isRedirect = code == HttpURLConnection.HTTP_MOVED_PERM ||
                code == HttpURLConnection.HTTP_MOVED_TEMP || code == 307 || code == 308
            val location = if (isRedirect) connection.getHeaderField("Location") else null
            if (location == null) return connection
            connection.disconnect()
            currentUrl = location
        }
        throw IllegalStateException("Too many redirects while downloading update.")
    }

    private inline fun copyWithProgress(
        input: java.io.InputStream,
        output: java.io.OutputStream,
        contentLength: Long,
        onPercentChanged: (percent: Int, bytesRead: Long) -> Unit
    ) {
        val buffer = ByteArray(16_384)
        var totalBytesRead = 0L
        var lastReportedPercent = -1
        while (true) {
            val bytesRead = input.read(buffer)
            if (bytesRead == -1) break
            output.write(buffer, 0, bytesRead)
            totalBytesRead += bytesRead

            val percent = if (contentLength > 0) {
                ((totalBytesRead * 100) / contentLength).toInt().coerceIn(0, 100)
            } else 0
            if (percent != lastReportedPercent) {
                lastReportedPercent = percent
                onPercentChanged(percent, totalBytesRead)
            }
        }
    }

    private inline fun <T> runCatchingNonCancellable(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (t: Throwable) {
        Result.failure(t)
    }
}
