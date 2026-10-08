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
import com.kitsune.app.core.storage.MediaFileUtils
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import org.json.JSONObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object YtDlpEngine {

    private const val TAG = "YtDlpEngine"
    private const val OUTPUT_PATH_SUFFIX = ".path"

    private val SPEED_REGEX = Regex("""at\s+([0-9.]+[kKMGT]?i?B/s)""")

    private val POST_PROCESSOR_TAGS = listOf(
        "[Merger]", "[ffmpeg]", "[ExtractAudio]", "[EmbedThumbnail]",
        "[EmbedSubtitle]", "[Metadata]", "[VideoConvertor]", "[FixupM3u8]"
    )

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


    private suspend fun <T> runKillable(processId: String, block: () -> T): T = coroutineScope {
        val work = async(Dispatchers.IO) { block() }
        try {
            work.await()
        } catch (cancellation: CancellationException) {
            runCatching { YoutubeDL.getInstance().destroyProcessById(processId) }
            throw cancellation
        }
    }

    private fun newProcessId(prefix: String): String = "${prefix}_${UUID.randomUUID()}"

    suspend fun fetchMediaInfo(context: Context, url: String): Result<MediaInfo> = withContext(Dispatchers.IO) {
        runCatchingNonCancellable {
            ensureInitialized(context).getOrThrow()
            val sanitized = UrlDetector.sanitizeUrl(url)
            val request = YoutubeDLRequest(sanitized).apply {
                addOption("--dump-json")
                addOption("--no-playlist")
                addOption("--no-warnings")
            }
            val processId = newProcessId("kitsune_info")
            val response = runKillable(processId) {
                YoutubeDL.getInstance().execute(request = request, processId = processId)
            }
            val jsonLine = response.out.lineSequence().firstOrNull { it.trimStart().startsWith("{") }
                ?: throw IllegalStateException("Empty metadata response.")
            val json = JSONObject(jsonLine)
            val platform = UrlDetector.detect(sanitized)

            MediaInfo(
                id = json.optString("id"),
                title = json.optString("title"),
                uploader = json.optString("uploader").ifBlank { json.optString("channel") },
                durationSeconds = json.optDouble("duration", 0.0).let { if (it.isNaN()) 0L else it.toLong() },
                thumbnailUrl = json.optString("thumbnail").ifBlank { null },
                platform = platform,
                originalUrl = sanitized
            )
        }
    }

    suspend fun fetchPlaylistInfo(context: Context, url: String): Result<PlaylistInfo> = withContext(Dispatchers.IO) {
        runCatchingNonCancellable {
            ensureInitialized(context).getOrThrow()
            val sanitized = UrlDetector.sanitizeUrl(url)
            val request = YoutubeDLRequest(sanitized).apply {
                addOption("--flat-playlist")
                addOption("-J")
                addOption("--no-warnings")
            }
            val processId = newProcessId("kitsune_playlist")
            val response = runKillable(processId) {
                YoutubeDL.getInstance().execute(request = request, processId = processId)
            }
            val json = JSONObject(response.out)

            val playlistTitle = json.optString("title").ifBlank { "Playlist" }
            val playlistAuthor = json.optString("uploader").ifBlank { json.optString("channel") }
            val playlistId = json.optString("id").ifBlank { "playlist" }

            val items = mutableListOf<PlaylistItem>()
            val entries = json.optJSONArray("entries")
            if (entries != null) {
                for (i in 0 until entries.length()) {
                    val entry = entries.optJSONObject(i) ?: continue
                    val itemId = entry.optString("id")
                    val itemUrl = resolvePlaylistEntryUrl(entry) ?: continue
                    val itemTitle = entry.optString("title").ifBlank { "Item ${i + 1}" }
                    val duration = entry.optDouble("duration", 0.0).let { if (it.isNaN()) 0L else it.toLong() }
                    val uploader = entry.optString("uploader")
                    val thumbnail = entry.optString("thumbnail").ifBlank {
                        val thumbs = entry.optJSONArray("thumbnails")
                        if (thumbs != null && thumbs.length() > 0) {
                            thumbs.optJSONObject(thumbs.length() - 1)?.optString("url")
                        } else null
                    }
                    items.add(
                        PlaylistItem(
                            id = itemId.ifBlank { itemUrl },
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

    private fun resolvePlaylistEntryUrl(entry: JSONObject): String? {
        val candidates = listOf(entry.optString("webpage_url"), entry.optString("url"))
        candidates.firstOrNull { it.startsWith("http://") || it.startsWith("https://") }?.let { return it }

        val itemId = entry.optString("id")
        val extractor = entry.optString("ie_key").ifBlank { entry.optString("extractor_key") }
        return if (itemId.isNotBlank() && extractor.equals("Youtube", ignoreCase = true)) {
            "https://www.youtube.com/watch?v=$itemId"
        } else {
            null
        }
    }

    suspend fun executeDownload(
        context: Context,
        url: String,
        config: DownloadConfig,
        outputDir: File,
        onProgressUpdate: (progress: Float, speed: String, eta: String, stage: DownloadStage) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatchingNonCancellable {
            ensureInitialized(context).getOrThrow()

            val cleanUrl = UrlDetector.sanitizeUrl(url)
            val platform = UrlDetector.detect(cleanUrl)

            if (!outputDir.exists()) outputDir.mkdirs()
            val outputPathFile = File(outputDir.parentFile, "${outputDir.name}$OUTPUT_PATH_SUFFIX")
            outputPathFile.delete()

            val request = YoutubeDLRequest(cleanUrl).apply {
                addOption("--no-mtime")
                addOption("--no-playlist")
                addOption("--retries", 3)
                addOption("--fragment-retries", 5)

                val outputTemplate = File(outputDir, "%(title).150B [%(id)s].%(ext)s").absolutePath
                addOption("-o", outputTemplate)
                addCommands(listOf("--print-to-file", "after_move:filepath", outputPathFile.absolutePath))

                if (config.audioOnly) {
                    addOption("-x")
                    addOption("--add-metadata")
                    when (config.audioCodec) {
                        AudioCodec.MP3 -> {
                            addOption("--audio-format", "mp3")
                            addOption("--audio-quality", config.audioQuality.ytDlpQuality)
                            addOption("--embed-thumbnail")
                            addOption("--convert-thumbnails", "jpg")
                        }
                        AudioCodec.OPUS -> {
                            addOption("--audio-format", "opus")
                            addOption("--audio-quality", config.audioQuality.ytDlpQuality)
                        }
                        AudioCodec.ORIGINAL -> {
                            addOption("-f", "bestaudio[ext=m4a]/bestaudio/best")
                        }
                    }
                } else if (config.muteAudio) {
                    addOption("-f", config.quality.ytDlpVideoOnlySelector)
                } else {
                    addOption("-f", config.quality.ytDlpFormatSelector)
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
            var destinationCount = 0
            val processId = newProcessId("kitsune_dl")

            runKillable(processId) {
                YoutubeDL.getInstance().execute(
                    request = request,
                    processId = processId
                ) { progress, etaInSeconds, line ->
                    val speed = extractSpeed(line)
                    val etaFormatted = if (etaInSeconds > 0) "${etaInSeconds}s" else ""

                    when {
                        line.contains("[download] Destination:") -> {
                            destinationCount++
                            currentStage = when {
                                config.audioOnly -> DownloadStage.AUDIO_STREAM
                                destinationCount >= 2 -> DownloadStage.AUDIO_STREAM
                                else -> DownloadStage.VIDEO_STREAM
                            }
                        }
                        POST_PROCESSOR_TAGS.any { line.contains(it) } -> {
                            currentStage = DownloadStage.FFMPEG_MUXING
                        }
                    }

                    onProgressUpdate(progress / 100f, speed, etaFormatted, currentStage)
                }
            }

            val reportedPath = outputPathFile.takeIf { it.exists() }
                ?.readLines()
                ?.lastOrNull { it.isNotBlank() }
                ?.trim()
            outputPathFile.delete()

            reportedPath?.let { File(it) }?.takeIf { it.isFile && it.length() > 0 }
                ?: MediaFileUtils.selectFinalOutputFile(outputDir.listFiles()?.toList().orEmpty())
                ?: throw IllegalStateException("Output media file not found after download completion.")
        }
    }

    private inline fun <T> runCatchingNonCancellable(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    private fun extractSpeed(logLine: String): String {
        val speedMatch = SPEED_REGEX.find(logLine)
        return speedMatch?.groupValues?.get(1) ?: ""
    }
}
