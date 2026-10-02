package com.kitsune.app.ui.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class CobaltColorTokens(
    val background: Color = Color(0xFF090A0F),
    val surface: Color = Color(0xFF13151F),
    val surfaceVariant: Color = Color(0xFF1C1E2D),
    val surfaceElevated: Color = Color(0xFF24273A),
    val accentCyan: Color = Color(0xFF00E5FF),
    val accentIndigo: Color = Color(0xFF6366F1),
    val textPrimary: Color = Color(0xFFF1F5F9),
    val textSecondary: Color = Color(0xFF94A3B8),
    val textMuted: Color = Color(0xFF64748B),
    val borderSubtle: Color = Color(0x1FFFFFFF),
    val success: Color = Color(0xFF10B981),
    val error: Color = Color(0xFFEF4444)
)

val LocalCobaltColors = staticCompositionLocalOf { CobaltColorTokens() }

@Immutable
data class CobaltSpacingTokens(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp
)

val LocalCobaltSpacing = staticCompositionLocalOf { CobaltSpacingTokens() }

@Immutable
data class CobaltShapeTokens(
    val pill: Dp = 999.dp,
    val cardRadius: Dp = 24.dp,
    val inputRadius: Dp = 16.dp,
    val sheetRadius: Dp = 28.dp,
    val smallRadius: Dp = 10.dp
)

val LocalCobaltShapes = staticCompositionLocalOf { CobaltShapeTokens() }
