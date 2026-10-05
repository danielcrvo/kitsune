package com.kitsune.app.ui.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class KitsuneColorTokens(
    val background: Color = Color(0xFF090A0F),
    val surface: Color = Color(0xFF0F121C),
    val surfaceVariant: Color = Color(0xFF161A26),
    val surfaceElevated: Color = Color(0xFF131722),
    val accentCyan: Color = Color(0xFF00F2FE),
    val accentIndigo: Color = Color(0xFF4FACFE),
    val accentOrange: Color = Color(0xFFF97316),
    val textPrimary: Color = Color(0xFFFFFFFF),
    val textSecondary: Color = Color(0xFFCBD5E1),
    val textMuted: Color = Color(0xFF94A3B8),
    val borderSubtle: Color = Color(0xFF222738),
    val borderFocus: Color = Color(0xFF00F2FE),
    val success: Color = Color(0xFF22C55E),
    val error: Color = Color(0xFFFF5252),
    val isAmoled: Boolean = false
) {
    val accentPrimary: Color get() = accentOrange
    val accentSecondary: Color get() = accentCyan
    val surfaceCard: Color get() = surfaceElevated
    val borderMuted: Color get() = borderSubtle
    companion object {
        fun defaultDark(): KitsuneColorTokens = KitsuneColorTokens()

        fun amoledDark(): KitsuneColorTokens = KitsuneColorTokens(
            background = Color(0xFF000000),
            surface = Color(0xFF07080D),
            surfaceVariant = Color(0xFF0D0F16),
            surfaceElevated = Color(0xFF12141C),
            borderSubtle = Color(0xFF1B1E29),
            borderFocus = Color(0xFF00F2FE),
            isAmoled = true
        )
    }
}

val LocalKitsuneColors = staticCompositionLocalOf { KitsuneColorTokens() }

@Immutable
data class KitsuneSpacingTokens(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp
)

val LocalKitsuneSpacing = staticCompositionLocalOf { KitsuneSpacingTokens() }

@Immutable
data class KitsuneShapeTokens(
    val pill: Dp = 999.dp,
    val cardRadius: Dp = 24.dp,
    val inputRadius: Dp = 16.dp,
    val sheetRadius: Dp = 28.dp,
    val smallRadius: Dp = 10.dp
)

val LocalKitsuneShapes = staticCompositionLocalOf { KitsuneShapeTokens() }
