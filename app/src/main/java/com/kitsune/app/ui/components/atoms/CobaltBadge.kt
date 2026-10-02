package com.kitsune.app.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitsune.app.core.model.PlatformType
import com.kitsune.app.ui.theme.KitsuneTheme

@Composable
fun CobaltBadge(
    label: String,
    modifier: Modifier = Modifier,
    badgeColor: Color = KitsuneTheme.colors.accentCyan,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .background(
                color = badgeColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(KitsuneTheme.shapes.pill)
            )
            .padding(horizontal = KitsuneTheme.spacing.sm, vertical = KitsuneTheme.spacing.xs)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Box(modifier = Modifier.padding(end = 4.dp)) {
                    leadingIcon()
                }
            }
            Text(
                text = label,
                style = KitsuneTheme.typography.labelSmall,
                color = badgeColor
            )
        }
    }
}

@Composable
fun PlatformBadge(
    platform: PlatformType,
    modifier: Modifier = Modifier
) {
    val color = Color(platform.brandHexColor)
    CobaltBadge(
        label = platform.displayName,
        modifier = modifier,
        badgeColor = color
    )
}

@Preview(name = "CobaltBadge Preview")
@Composable
private fun CobaltBadgePreview() {
    KitsuneTheme {
        PlatformBadge(platform = PlatformType.YOUTUBE)
    }
}
