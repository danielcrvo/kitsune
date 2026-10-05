package com.kitsune.app.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kitsune.app.R
import com.kitsune.app.core.model.VideoQuality
import com.kitsune.app.ui.components.atoms.KitsuneBadge
import com.kitsune.app.ui.theme.KitsuneTheme

@Composable
fun QualityOptionTile(
    quality: VideoQuality,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(KitsuneTheme.shapes.smallRadius)
    val borderColor = if (isSelected) KitsuneTheme.colors.accentCyan else KitsuneTheme.colors.borderSubtle
    val bgColor = if (isSelected) KitsuneTheme.colors.surfaceElevated else KitsuneTheme.colors.surfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onSelect)
            .padding(KitsuneTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(2.dp, if (isSelected) KitsuneTheme.colors.accentCyan else KitsuneTheme.colors.textMuted, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(KitsuneTheme.colors.accentCyan)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = KitsuneTheme.spacing.md)
        ) {
            Text(
                text = quality.label,
                style = KitsuneTheme.typography.titleMedium,
                color = if (isSelected) KitsuneTheme.colors.textPrimary else KitsuneTheme.colors.textSecondary
            )
            if (quality.maxResolution > 0) {
                Text(
                    text = stringResource(R.string.quality_max_res, quality.maxResolution),
                    style = KitsuneTheme.typography.labelSmall,
                    color = KitsuneTheme.colors.textMuted
                )
            }
        }

        if (quality == VideoQuality.AUTO || quality == VideoQuality.Q_2160P) {
            KitsuneBadge(
                label = if (quality == VideoQuality.Q_2160P) "4K UHD" else stringResource(R.string.badge_recommended),
                badgeColor = KitsuneTheme.colors.accentCyan
            )
        }
    }
}
