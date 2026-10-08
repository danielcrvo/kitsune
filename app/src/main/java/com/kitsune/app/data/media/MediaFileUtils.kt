package com.kitsune.app.data.media

import java.io.File

object MediaFileUtils {

    const val VIDEO_FOLDER = "Movies/Kitsune"
    const val AUDIO_FOLDER = "Music/Kitsune"

    private const val MAX_FILE_NAME_LENGTH = 120

    private val ILLEGAL_FILE_NAME_CHARS = Regex("""[\\/:*?"<>|\u0000-\u001F]""")
    private val REPEATED_WHITESPACE = Regex("""\s+""")

    private val INTERMEDIATE_EXTENSIONS = setOf(
        "part", "ytdl", "temp", "tmp", "vtt", "srt", "ass", "lrc",
        "jpg", "jpeg", "png", "webp", "json", "description", "txt"
    )

    private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "webm", "avi", "mov", "m4v", "3gp", "flv")

    private val AUDIO_MIME_TYPES = mapOf(
        "mp3" to "audio/mpeg",
        "m4a" to "audio/mp4",
        "aac" to "audio/aac",
        "opus" to "audio/ogg",
        "ogg" to "audio/ogg",
        "oga" to "audio/ogg",
        "flac" to "audio/flac",
        "wav" to "audio/x-wav",
        "webm" to "audio/webm",
        "mka" to "audio/x-matroska"
    )

    private val VIDEO_MIME_TYPES = mapOf(
        "mp4" to "video/mp4",
        "m4v" to "video/mp4",
        "webm" to "video/webm",
        "mkv" to "video/x-matroska",
        "mov" to "video/quicktime",
        "avi" to "video/x-msvideo",
        "3gp" to "video/3gpp",
        "flv" to "video/x-flv"
    )

    fun sanitizeFileName(rawName: String, maxLength: Int = MAX_FILE_NAME_LENGTH): String {
        val cleaned = rawName
            .replace(REPEATED_WHITESPACE, " ")
            .replace(ILLEGAL_FILE_NAME_CHARS, "_")
            .trim()
            .trim('.')
            .trim()
        if (cleaned.length <= maxLength) return cleaned
        val cut = if (Character.isHighSurrogate(cleaned[maxLength - 1])) maxLength - 1 else maxLength
        return cleaned.substring(0, cut).trim()
    }

    fun isVideoExtension(extension: String): Boolean = extension.lowercase() in VIDEO_EXTENSIONS

    fun fallbackMimeType(extension: String, isAudio: Boolean): String {
        val ext = extension.lowercase()
        return if (isAudio) {
            AUDIO_MIME_TYPES[ext] ?: "audio/mpeg"
        } else {
            VIDEO_MIME_TYPES[ext] ?: "video/mp4"
        }
    }

    fun relativeFolder(isAudio: Boolean): String = if (isAudio) AUDIO_FOLDER else VIDEO_FOLDER

    fun isIntermediateFile(file: File): Boolean {
        val name = file.name.lowercase()
        if (name.endsWith(".part") || name.contains(".part-frag")) return true
        if (Regex("""\.f\d+\.[a-z0-9]+$""").containsMatchIn(name)) return true
        return file.extension.lowercase() in INTERMEDIATE_EXTENSIONS
    }

    fun selectFinalOutputFile(candidates: List<File>): File? {
        return candidates
            .filter { it.isFile && it.length() > 0 && !isIntermediateFile(it) }
            .maxByOrNull { it.length() }
    }

    fun uniqueFile(directory: File, baseName: String, extension: String): File {
        val suffix = if (extension.isNotBlank()) ".$extension" else ""
        var candidate = File(directory, "$baseName$suffix")
        var index = 1
        while (candidate.exists()) {
            candidate = File(directory, "$baseName ($index)$suffix")
            index++
        }
        return candidate
    }
}
