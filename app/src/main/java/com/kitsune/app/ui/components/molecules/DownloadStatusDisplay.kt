package com.kitsune.app.ui.components.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kitsune.app.R
import com.kitsune.app.core.model.DownloadStage
import com.kitsune.app.ui.components.atoms.KitsuneBadge
import com.kitsune.app.ui.components.atoms.KitsuneLinearGauge
import com.kitsune.app.ui.theme.KitsuneTheme

@Composable
fun DownloadStatusDisplay(
    progress: Float,
    stage: DownloadStage,
    speed: String,
    eta: String,
    modifier: Modifier = Modifier
) {
    val progressPercent = (progress * 100).toInt().coerceIn(0, 100)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(stage.labelRes),
                style = KitsuneTheme.typography.bodyMedium,
                color = KitsuneTheme.colors.textPrimary
            )
            Text(
                text = "$progressPercent%",
                style = KitsuneTheme.typography.labelLarge,
                color = KitsuneTheme.colors.accentCyan
            )
        }

        KitsuneLinearGauge(
            progress = progress,
            modifier = Modifier.padding(vertical = KitsuneTheme.spacing.sm)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (speed.isNotEmpty()) {
                KitsuneBadge(
                    label = speed,
                    badgeColor = KitsuneTheme.colors.accentIndigo
                )
            }
            if (eta.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.status_eta, eta),
                    style = KitsuneTheme.typography.labelSmall,
                    color = KitsuneTheme.colors.textMuted
                )
            }
        }
    }
}
