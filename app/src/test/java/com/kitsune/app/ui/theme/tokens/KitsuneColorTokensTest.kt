package com.kitsune.app.ui.theme.tokens

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KitsuneColorTokensTest {

    @Test
    fun defaultDarkTokensHaveExpectedValues() {
        val tokens = KitsuneColorTokens.defaultDark()

        assertFalse(tokens.isAmoled)
        assertEquals(Color(0xFF090A0F), tokens.background)
        assertEquals(Color(0xFF0F121C), tokens.surface)
        assertEquals(Color(0xFF161A26), tokens.surfaceVariant)
        assertEquals(Color(0xFF131722), tokens.surfaceElevated)
        assertEquals(Color(0xFF00F2FE), tokens.accentCyan)
        assertEquals(Color(0xFF4FACFE), tokens.accentIndigo)
        assertEquals(Color(0xFFF97316), tokens.accentOrange)
        assertEquals(Color(0xFFF97316), tokens.accentPrimary)
        assertEquals(Color(0xFF00F2FE), tokens.accentSecondary)
    }

    @Test
    fun amoledDarkTokensHaveExpectedValues() {
        val tokens = KitsuneColorTokens.amoledDark()

        assertTrue(tokens.isAmoled)
        assertEquals(Color(0xFF000000), tokens.background)
        assertEquals(Color(0xFF07080D), tokens.surface)
        assertEquals(Color(0xFF0D0F16), tokens.surfaceVariant)
        assertEquals(Color(0xFF12141C), tokens.surfaceElevated)
        assertEquals(Color(0xFF1B1E29), tokens.borderSubtle)
        assertEquals(Color(0xFF00F2FE), tokens.borderFocus)
    }

    @Test
    fun fromColorSchemeStandardDarkMapsProperly() {
        val testScheme = darkColorScheme(
            primary = Color(0xFFAABBCC),
            secondary = Color(0xFF112233),
            tertiary = Color(0xFF445566),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF2C2C2C),
            surfaceContainer = Color(0xFF222222),
            surfaceContainerHigh = Color(0xFF2A2A2A),
            onSurface = Color(0xFFEEEEEE),
            onSurfaceVariant = Color(0xFFCCCCCC),
            outline = Color(0xFF888888),
            outlineVariant = Color(0xFF555555),
            error = Color(0xFFFF4444)
        )

        val tokens = KitsuneColorTokens.fromColorScheme(testScheme, isAmoled = false)

        assertFalse(tokens.isAmoled)
        assertEquals(Color(0xFFAABBCC), tokens.accentOrange)
        assertEquals(Color(0xFFAABBCC), tokens.accentPrimary)
        assertEquals(Color(0xFF112233), tokens.accentCyan)
        assertEquals(Color(0xFF112233), tokens.accentSecondary)
        assertEquals(Color(0xFF445566), tokens.accentIndigo)
        assertEquals(Color(0xFF121212), tokens.background)
        assertEquals(Color(0xFF1E1E1E), tokens.surface)
        assertEquals(Color(0xFF222222), tokens.surfaceElevated)
        assertEquals(Color(0xFFEEEEEE), tokens.textPrimary)
        assertEquals(Color(0xFFCCCCCC), tokens.textSecondary)
        assertEquals(Color(0xFF888888), tokens.textMuted)
        assertEquals(Color(0xFFAABBCC), tokens.borderFocus)
        assertEquals(Color(0xFFFF4444), tokens.error)
    }

    @Test
    fun fromColorSchemeAmoledDarkForcesPureBlackBackground() {
        val testScheme = darkColorScheme(
            primary = Color(0xFFAABBCC),
            secondary = Color(0xFF112233),
            tertiary = Color(0xFF445566),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            surfaceContainer = Color(0xFF222222),
            surfaceContainerLow = Color(0xFF1A1A1A),
            surfaceContainerLowest = Color(0xFF0F0F0F),
            onSurface = Color(0xFFEEEEEE),
            onSurfaceVariant = Color(0xFFCCCCCC),
            outline = Color(0xFF888888),
            outlineVariant = Color(0xFF555555),
            error = Color(0xFFFF4444)
        )

        val tokens = KitsuneColorTokens.fromColorScheme(testScheme, isAmoled = true)

        assertTrue(tokens.isAmoled)
        assertEquals(Color(0xFF000000), tokens.background)
        assertEquals(Color(0xFF0F0F0F), tokens.surface)
        assertEquals(Color(0xFF1A1A1A), tokens.surfaceVariant)
        assertEquals(Color(0xFF222222), tokens.surfaceElevated)
        assertEquals(Color(0xFFAABBCC), tokens.accentOrange)
        assertEquals(Color(0xFFAABBCC), tokens.accentPrimary)
        assertEquals(Color(0xFF112233), tokens.accentCyan)
        assertEquals(Color(0xFF112233), tokens.accentSecondary)
        assertEquals(Color(0xFF445566), tokens.accentIndigo)
        assertEquals(Color(0xFFEEEEEE), tokens.textPrimary)
        assertEquals(Color(0xFFCCCCCC), tokens.textSecondary)
        assertEquals(Color(0xFF888888), tokens.textMuted)
        assertEquals(Color(0xFFAABBCC), tokens.borderFocus)
        assertEquals(Color(0xFFFF4444), tokens.error)
    }
}
