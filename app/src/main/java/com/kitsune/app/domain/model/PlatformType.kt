package com.kitsune.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
enum class PlatformType(
    val displayName: String,
    val brandHexColor: Long,
    val iconName: String
) {
    YOUTUBE("YouTube", 0xFFFF0000, "youtube"),
    TIKTOK("TikTok", 0xFF00F2FE, "tiktok"),
    INSTAGRAM("Instagram", 0xFFE1306C, "instagram"),
    BILIBILI("Bilibili", 0xFF00A1D6, "bilibili"),
    TWITTER("X / Twitter", 0xFF1DA1F2, "twitter"),
    REDDIT("Reddit", 0xFFFF4500, "reddit"),
    SOUNDCLOUD("SoundCloud", 0xFFFF5500, "soundcloud"),
    PINTEREST("Pinterest", 0xFFE60023, "pinterest"),
    UNKNOWN("Web Media", 0xFF7C5CFF, "generic")
}
