package com.kitsune.app.core.engine

import android.content.Context
import android.util.Log
import com.kitsune.app.core.model.AudioCodec
import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.DownloadStage
import com.kitsune.app.core.model.MediaInfo
import com.kitsune.app.core.model.PlatformType
import com.kitsune.app.core.model.PlaylistInfo
import com.kitsune.app.core.model.PlaylistItem
import com.kitsune.app.core.model.VideoQuality
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

object YtDlpEngine {

    private const val TAG = "YtDlpEngine"

    @Volatile
    private var isInitialized = false
    private val initMutex = Mutex()


    suspend fun ensureInitialized(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext Result.success(Unit)
        initMutex.withLock {
            if (isInitialized) return@withLock Result.success(Unit)
            try {
                val appContext = context.applicationContext
                Log.d(TAG, "Unpacking and initializing native YoutubeDL binaries...")
                YoutubeDL.getInstance().init(appContext)
                try {
                    FFmpeg.getInstance().init(appContext)
                } catch (ffmpegErr: Throwable) {
                    Log.w(TAG, "Non-fatal warning initializing FFmpeg", ffmpegErr)
                }
                isInitialized = true
                Log.i(TAG, "YoutubeDL engine successfully initialized.")
                Result.success(Unit)
            } catch (e: Throwable) {
                Log.e(TAG, "Fatal failure initializing YoutubeDL engine", e)
                Result.failure(e)
            }
        }
    }


    suspend fun initialize(context: Context): Result<Unit> = ensureInitialized(context)


    suspend fun fetchMediaInfo(context: Context, url: String): Result<MediaInfo> = withContext(Dispatchers.IO) {
        runCatching {
            ensureInitialized(context).getOrThrow()
            val sanitized = UrlDetector.sanitizeUrl(url)
            val info = YoutubeDL.getInstance().getInfo(sanitized)
            val platform = UrlDetector.detect(sanitized)

            MediaInfo(
                id = info.id ?: "",
                title = info.title ?: "Mídia Kitsune",
                uploader = info.uploader ?: "Desconhecido",
                durationSeconds = info.duration.toLong(),
                thumbnailUrl = info.thumbnail,
                platform = platform,
                originalUrl = sanitized
            )
        }
    }

    suspend fun fetchPlaylistInfo(context: Context, url: String): Result<PlaylistInfo> = withContext(Dispatchers.IO) {
        runCatching {
            ensureInitialized(context).getOrThrow()
            val sanitized = UrlDetector.sanitizeUrl(url)
            val request = YoutubeDLRequest(sanitized).apply {
                addOption("--flat-playlist")
                addOption("-J")
                addOption("--no-warnings")
            }
            val response = YoutubeDL.getInstance().execute(request)
            val json = JSONObject(response.out)

            val playlistTitle = json.optString("title").ifBlank { "Playlist Kitsune" }
            val playlistAuthor = json.optString("uploader").ifBlank { json.optString("channel") }
            val playlistId = json.optString("id").ifBlank { "playlist" }

            val items = mutableListOf<PlaylistItem>()
            val entries = json.optJSONArray("entries")
            if (entries != null) {
                for (i in 0 until entries.length()) {
                    val entry = entries.optJSONObject(i) ?: continue
                    val itemId = entry.optString("id")
                    val itemTitle = entry.optString("title").ifBlank { "Item ${i + 1}" }
                    val itemUrl = entry.optString("url").let { u ->
                        if (u.startsWith("http")) u else "https://www.youtube.com/watch?v=$itemId"
                    }
                    val duration = entry.optLong("duration", 0L)
                    val uploader = entry.optString("uploader")
                    val thumbnail = entry.optString("thumbnail").ifBlank {
                        val thumbs = entry.optJSONArray("thumbnails")
                        if (thumbs != null && thumbs.length() > 0) {
                            thumbs.optJSONObject(thumbs.length() - 1)?.optString("url")
                        } else null
                    }
                    items.add(
                        PlaylistItem(
                            id = itemId,
                            title = itemTitle,
                            url = itemUrl,
                            durationSeconds = duration,
                            uploader = uploader,
                            thumbnailUrl = thumbnail,
                            index = i + 1
                        )
                    )
                }
            }

            PlaylistInfo(
                id = playlistId,
                title = playlistTitle,
                author = playlistAuthor,
                originalUrl = sanitized,
                items = items
            )
        }
    }


    suspend fun executeDownload(
        context: Context,
        url: String,
        config: DownloadConfig,
        outputDir: File,
        onProgressUpdate: (progress: Float, speed: String, eta: String, stage: DownloadStage) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            ensureInitialized(context).getOrThrow()

            val cleanUrl = UrlDetector.sanitizeUrl(url)
            val platform = UrlDetector.detect(cleanUrl)

            if (!outputDir.exists()) outputDir.mkdirs()

            val request = YoutubeDLRequest(cleanUrl).apply {
                addOption("--no-mtime")
                addOption("--no-playlist")
                addOption("--retries", 3)
                addOption("--fragment-retries", 5)


                val outputTemplate = File(outputDir, "kitsune_%(id)s.%(ext)s").absolutePath
                addOption("-o", outputTemplate)


                if (config.audioOnly) {
                    addOption("-x")
                    addOption("--add-metadata")
                    addOption("--embed-thumbnail")
                    when (config.audioCodec) {
                        AudioCodec.MP3 -> {
                            addOption("--audio-format", "mp3")
                            addOption("--audio-quality", config.audioQuality.ytDlpQuality)
                        }
                        AudioCodec.OPUS -> {
                            addOption("--audio-format", "opus")
                            addOption("--audio-quality", config.audioQuality.ytDlpQuality)
                        }
                        AudioCodec.ORIGINAL -> {
                            addOption("-f", "bestaudio/best")
                        }
                    }
                } else if (config.muteAudio) {
                    val formatSelector = when (config.quality) {
                        VideoQuality.AUTO -> "bestvideo/best"
                        VideoQuality.Q_2160P -> "bestvideo[height<=2160]/best"
                        VideoQuality.Q_1440P -> "bestvideo[height<=1440]/best"
                        VideoQuality.Q_1080P -> "bestvideo[height<=1080]/best"
                        VideoQuality.Q_720P -> "bestvideo[height<=720]/best"
                        VideoQuality.Q_480P -> "bestvideo[height<=480]/best"
                        VideoQuality.AUDIO_ONLY -> "bestvideo/best"
                    }
                    addOption("-f", formatSelector)
                } else {
                    val formatSelector = when (config.quality) {
                        VideoQuality.AUTO -> "bestvideo+bestaudio/best"
                        VideoQuality.Q_2160P -> "bestvideo[height<=2160]+bestaudio/best[height<=2160]/best"
                        VideoQuality.Q_1440P -> "bestvideo[height<=1440]+bestaudio/best[height<=1440]/best"
                        VideoQuality.Q_1080P -> "bestvideo[height<=1080]+bestaudio/best[height<=1080]/best"
                        VideoQuality.Q_720P -> "bestvideo[height<=720]+bestaudio/best[height<=720]/best"
                        VideoQuality.Q_480P -> "bestvideo[height<=480]+bestaudio/best[height<=480]/best"
                        VideoQuality.AUDIO_ONLY -> "bestaudio/best"
                    }
                    addOption("-f", formatSelector)
                    addOption("--merge-output-format", "mp4")
                }


                when (platform) {
                    PlatformType.TIKTOK -> {
                        addOption("--add-header", "Referer:https://www.tiktok.com/")
                    }
                    PlatformType.INSTAGRAM -> {
                        addOption("--add-header", "User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    }
                    PlatformType.BILIBILI -> {
                        addOption("--add-header", "Referer:https://www.bilibili.com/")
                    }
                    PlatformType.TWITTER -> {
                        addOption("--add-header", "User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    }
                    else -> {}
                }

                if (config.embedSubtitles && !config.audioOnly) {
                    addOption("--write-subs")
                    addOption("--embed-subs")
                }
            }

            var currentStage = DownloadStage.INITIALIZING

            val response: YoutubeDLResponse = YoutubeDL.getInstance().execute(
                request = request,
                processId = "kitsune_dl_${System.currentTimeMillis()}"
            ) { progress, etaInSeconds, line ->
                val speed = extractSpeed(line)
                val etaFormatted = if (etaInSeconds > 0) "${etaInSeconds}s" else ""

                if (line.contains("[download] Destination", ignoreCase = true) || line.contains("Destination: ", ignoreCase = true)) {
                    currentStage = if (line.contains(".f137.") || line.contains("video", ignoreCase = true)) {
                        DownloadStage.VIDEO_STREAM
                    } else if (line.contains(".f140.") || line.contains("audio", ignoreCase = true)) {
                        DownloadStage.AUDIO_STREAM
                    } else {
                        DownloadStage.VIDEO_STREAM
                    }
                } else if (line.contains("[Merger]") || line.contains("[ffmpeg]")) {
                    currentStage = DownloadStage.FFMPEG_MUXING
                }

                onProgressUpdate(progress / 100f, speed, etaFormatted, currentStage)
            }

            val downloadedFiles = outputDir.listFiles()?.filter {
                it.isFile && it.name.startsWith("kitsune_")
            } ?: emptyList()

            downloadedFiles.maxByOrNull { it.lastModified() }
                ?: throw IllegalStateException("Output media file not found after download completion.")
        }
    }

    private fun extractSpeed(logLine: String): String {
        val speedMatch = Regex("""at\s+([0-9.]+[kKMGT]?i?B/s)""").find(logLine)
        return speedMatch?.groupValues?.get(1) ?: ""
    }
}
