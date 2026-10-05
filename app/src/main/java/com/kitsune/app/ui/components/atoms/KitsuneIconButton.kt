package com.kitsune.app.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kitsune.app.ui.theme.KitsuneTheme

@Composable
fun KitsuneIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    containerColor: Color = KitsuneTheme.colors.surfaceElevated,
    contentColor: Color = KitsuneTheme.colors.textPrimary,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview(name = "KitsuneIconButton Preview")
@Composable
private fun KitsuneIconButtonPreview() {
    KitsuneTheme {
        KitsuneIconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = KitsuneTheme.colors.textSecondary
            )
        }
    }
}
