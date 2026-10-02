package com.kitsune.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.kitsune.app.ui.theme.tokens.CobaltColorTokens
import com.kitsune.app.ui.theme.tokens.CobaltShapeTokens
import com.kitsune.app.ui.theme.tokens.CobaltSpacingTokens
import com.kitsune.app.ui.theme.tokens.LocalCobaltColors
import com.kitsune.app.ui.theme.tokens.LocalCobaltShapes
import com.kitsune.app.ui.theme.tokens.LocalCobaltSpacing

private val DarkColorScheme = darkColorScheme(
    primary = CobaltColorTokens().accentCyan,
    onPrimary = CobaltColorTokens().background,
    primaryContainer = CobaltColorTokens().surfaceElevated,
    onPrimaryContainer = CobaltColorTokens().accentCyan,
    secondary = CobaltColorTokens().accentIndigo,
    onSecondary = CobaltColorTokens().textPrimary,
    background = CobaltColorTokens().background,
    onBackground = CobaltColorTokens().textPrimary,
    surface = CobaltColorTokens().surface,
    onSurface = CobaltColorTokens().textPrimary,
    surfaceVariant = CobaltColorTokens().surfaceVariant,
    onSurfaceVariant = CobaltColorTokens().textSecondary,
    error = CobaltColorTokens().error,
    onError = CobaltColorTokens().textPrimary
)

@Composable
fun KitsuneTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicDarkColorScheme(context)
        }
        else -> DarkColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            activity?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    val cobaltColors = CobaltColorTokens()
    val cobaltSpacing = CobaltSpacingTokens()
    val cobaltShapes = CobaltShapeTokens()

    CompositionLocalProvider(
        LocalCobaltColors provides cobaltColors,
        LocalCobaltSpacing provides cobaltSpacing,
        LocalCobaltShapes provides cobaltShapes
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KitsuneTypography,
            content = content
        )
    }
}

object KitsuneTheme {
    val colors: CobaltColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalCobaltColors.current

    val spacing: CobaltSpacingTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalCobaltSpacing.current

    val shapes: CobaltShapeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalCobaltShapes.current

    val typography: androidx.compose.material3.Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
