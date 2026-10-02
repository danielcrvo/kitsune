package com.kitsune.app.ui.components.atoms

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitsune.app.ui.theme.KitsuneTheme

enum class CobaltButtonVariant {
    PRIMARY,
    SECONDARY,
    ACCENT
}

@Composable
fun CobaltButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    variant: CobaltButtonVariant = CobaltButtonVariant.PRIMARY,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = KitsuneTheme.spacing.lg,
        vertical = KitsuneTheme.spacing.md
    ),
    content: @Composable RowScope.() -> Unit
) {
    val containerColor = when (variant) {
        CobaltButtonVariant.PRIMARY -> KitsuneTheme.colors.accentCyan
        CobaltButtonVariant.SECONDARY -> KitsuneTheme.colors.surfaceElevated
        CobaltButtonVariant.ACCENT -> KitsuneTheme.colors.accentIndigo
    }

    val contentColor = when (variant) {
        CobaltButtonVariant.PRIMARY -> KitsuneTheme.colors.background
        CobaltButtonVariant.SECONDARY -> KitsuneTheme.colors.textPrimary
        CobaltButtonVariant.ACCENT -> Color.White
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(KitsuneTheme.shapes.pill),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = KitsuneTheme.colors.surfaceVariant,
            disabledContentColor = KitsuneTheme.colors.textMuted
        ),
        contentPadding = contentPadding
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            content()
        }
    }
}

@Preview(name = "CobaltButton Preview")
@Composable
private fun CobaltButtonPreview() {
    KitsuneTheme {
        CobaltButton(onClick = {}) {
            Text("Baixar Vídeo")
        }
    }
}
