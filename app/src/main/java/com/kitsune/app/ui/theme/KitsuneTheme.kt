package com.kitsune.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.kitsune.app.ui.theme.tokens.KitsuneColorTokens
import com.kitsune.app.ui.theme.tokens.KitsuneShapeTokens
import com.kitsune.app.ui.theme.tokens.KitsuneSpacingTokens
import com.kitsune.app.ui.theme.tokens.LocalKitsuneColors
import com.kitsune.app.ui.theme.tokens.LocalKitsuneShapes
import com.kitsune.app.ui.theme.tokens.LocalKitsuneSpacing

private fun buildDarkColorScheme(tokens: KitsuneColorTokens) = darkColorScheme(
    primary = tokens.accentCyan,
    onPrimary = tokens.background,
    primaryContainer = tokens.surfaceElevated,
    onPrimaryContainer = tokens.accentCyan,
    secondary = tokens.accentIndigo,
    onSecondary = tokens.textPrimary,
    background = tokens.background,
    onBackground = tokens.textPrimary,
    surface = tokens.surface,
    onSurface = tokens.textPrimary,
    surfaceVariant = tokens.surfaceVariant,
    onSurfaceVariant = tokens.textSecondary,
    error = tokens.error,
    onError = tokens.textPrimary
)

@Composable
fun KitsuneTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    isAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val kitsuneColors = if (isAmoled) {
        KitsuneColorTokens.amoledDark()
    } else {
        KitsuneColorTokens.defaultDark()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicDarkColorScheme(context)
        }
        else -> buildDarkColorScheme(kitsuneColors)
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

    val kitsuneSpacing = KitsuneSpacingTokens()
    val kitsuneShapes = KitsuneShapeTokens()

    CompositionLocalProvider(
        LocalKitsuneColors provides kitsuneColors,
        LocalKitsuneSpacing provides kitsuneSpacing,
        LocalKitsuneShapes provides kitsuneShapes
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KitsuneTypography,
            content = content
        )
    }
}

object KitsuneTheme {
    val colors: KitsuneColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalKitsuneColors.current

    val spacing: KitsuneSpacingTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalKitsuneSpacing.current

    val shapes: KitsuneShapeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalKitsuneShapes.current

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
