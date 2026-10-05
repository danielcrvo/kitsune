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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitsune.app.ui.theme.KitsuneTheme

enum class KitsuneButtonVariant {
    PRIMARY,
    SECONDARY,
    ACCENT
}

@Composable
fun KitsuneButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    variant: KitsuneButtonVariant = KitsuneButtonVariant.PRIMARY,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = KitsuneTheme.spacing.lg,
        vertical = KitsuneTheme.spacing.md
    ),
    content: @Composable RowScope.() -> Unit
) {
    val containerColor = when (variant) {
        KitsuneButtonVariant.PRIMARY -> KitsuneTheme.colors.accentCyan
        KitsuneButtonVariant.SECONDARY -> KitsuneTheme.colors.surfaceElevated
        KitsuneButtonVariant.ACCENT -> KitsuneTheme.colors.accentIndigo
    }

    val contentColor = when (variant) {
        KitsuneButtonVariant.PRIMARY -> KitsuneTheme.colors.background
        KitsuneButtonVariant.SECONDARY -> KitsuneTheme.colors.textPrimary
        KitsuneButtonVariant.ACCENT -> KitsuneTheme.colors.textPrimary
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

@Preview(name = "KitsuneButton Preview")
@Composable
private fun KitsuneButtonPreview() {
    KitsuneTheme {
        KitsuneButton(onClick = {}) {
            Text("Baixar Vídeo")
        }
    }
}
