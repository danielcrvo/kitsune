package com.kitsune.app.core.engine

import com.kitsune.app.core.model.PlatformType
import java.net.URI

object UrlDetector {

    private val YOUTUBE_REGEX = Regex(
        """^(https?://)?(www\.|m\.)?(youtube\.com/(watch\?v=|shorts/|embed/|v/)|youtu\.be/)[a-zA-Z0-9_-]+""",
        RegexOption.IGNORE_CASE
    )

    private val TIKTOK_REGEX = Regex(
        """^(https?://)?(www\.|vm\.|vt\.)?tiktok\.com/.*""",
        RegexOption.IGNORE_CASE
    )

    private val INSTAGRAM_REGEX = Regex(
        """^(https?://)?(www\.)?instagram\.com/(p|reel|tv)/[a-zA-Z0-9_-]+""",
        RegexOption.IGNORE_CASE
    )

    private val BILIBILI_REGEX = Regex(
        """^(https?://)?(www\.|m\.)?(bilibili\.com/video/BV|b23\.tv/)[a-zA-Z0-9_-]+""",
        RegexOption.IGNORE_CASE
    )

    private val TWITTER_REGEX = Regex(
        """^(https?://)?(www\.)?(twitter\.com|x\.com)/[a-zA-Z0-9_]+/status/[0-9]+""",
        RegexOption.IGNORE_CASE
    )

    private val REDDIT_REGEX = Regex(
        """^(https?://)?(www\.|v\.)?(reddit\.com/r/[a-zA-Z0-9_]+/comments/|redd\.it/)[a-zA-Z0-9_-]+""",
        RegexOption.IGNORE_CASE
    )

    private val SOUNDCLOUD_REGEX = Regex(
        """^(https?://)?(www\.|m\.)?(soundcloud\.com)/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+""",
        RegexOption.IGNORE_CASE
    )

    private val PINTEREST_REGEX = Regex(
        """^(https?://)?(www\.)?(pinterest\.[a-z.]+|pin\.it)/.+""",
        RegexOption.IGNORE_CASE
    )


    fun detect(url: String): PlatformType {
        val trimmed = url.trim()
        return when {
            YOUTUBE_REGEX.containsMatchIn(trimmed) -> PlatformType.YOUTUBE
            TIKTOK_REGEX.containsMatchIn(trimmed) -> PlatformType.TIKTOK
            INSTAGRAM_REGEX.containsMatchIn(trimmed) -> PlatformType.INSTAGRAM
            BILIBILI_REGEX.containsMatchIn(trimmed) -> PlatformType.BILIBILI
            TWITTER_REGEX.containsMatchIn(trimmed) -> PlatformType.TWITTER
            REDDIT_REGEX.containsMatchIn(trimmed) -> PlatformType.REDDIT
            SOUNDCLOUD_REGEX.containsMatchIn(trimmed) -> PlatformType.SOUNDCLOUD
            PINTEREST_REGEX.containsMatchIn(trimmed) -> PlatformType.PINTEREST
            isValidGenericUrl(trimmed) -> PlatformType.UNKNOWN
            else -> PlatformType.UNKNOWN
        }
    }

    fun isPlaylistUrl(url: String): Boolean {
        if (!isValidUrl(url)) return false
        val lower = url.lowercase().trim()
        return lower.contains("list=") ||
                lower.contains("/sets/") ||
                lower.contains("/album/") ||
                lower.contains("/albums/")
    }


    fun isValidUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return false
        return try {
            val uri = URI(trimmed)
            (uri.scheme == "http" || uri.scheme == "https") && !uri.host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    private fun isValidGenericUrl(url: String): Boolean {
        return isValidUrl(url)
    }


    private val TRACKING_PARAMS = setOf("si", "igsh", "igshid", "share_id", "ref_src", "fbclid", "gclid")

    private val TWITTER_TRACKING_PARAMS = setOf("s", "t")

    private fun isTwitterHost(host: String?): Boolean {
        val lower = host?.lowercase() ?: return false
        return lower == "twitter.com" || lower.endsWith(".twitter.com") ||
            lower == "x.com" || lower.endsWith(".x.com")
    }

    fun sanitizeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        return try {
            val uri = URI(trimmed)
            val rawQuery = uri.rawQuery
            if (rawQuery.isNullOrBlank() || uri.scheme.isNullOrBlank() || uri.rawAuthority.isNullOrBlank()) {
                return trimmed
            }

            val isTwitter = isTwitterHost(uri.host)
            val cleanQuery = rawQuery
                .split("&")
                .filter { it.isNotEmpty() }
                .filterNot { param ->
                    val key = param.substringBefore("=").lowercase()
                    key.startsWith("utm_") || key in TRACKING_PARAMS ||
                        (isTwitter && key in TWITTER_TRACKING_PARAMS)
                }
                .joinToString("&")

            val queryPart = if (cleanQuery.isNotEmpty()) "?$cleanQuery" else ""
            val fragmentPart = uri.rawFragment?.let { "#$it" } ?: ""
            "${uri.scheme}://${uri.rawAuthority}${uri.rawPath.orEmpty()}$queryPart$fragmentPart"
        } catch (_: Exception) {
            trimmed
        }
    }
}
