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


    fun sanitizeUrl(rawUrl: String): String {
        return try {
            val uri = URI(rawUrl.trim())
            if (uri.query.isNullOrBlank()) return rawUrl.trim()

            val cleanQuery = uri.query
                .split("&")
                .filterNot { param ->
                    val key = param.substringBefore("=").lowercase()
                    key.startsWith("utm_") || key == "si" || key == "igsh" ||
                        key == "share_id" || key == "ref_src" || key == "fbclid" ||
                        key == "s" || key == "t"
                }
                .joinToString("&")

            val port = if (uri.port != -1) ":${uri.port}" else ""
            val queryPart = if (cleanQuery.isNotEmpty()) "?$cleanQuery" else ""
            "${uri.scheme}://${uri.host}$port${uri.path}$queryPart"
        } catch (_: Exception) {
            rawUrl.trim()
        }
    }
}
