package com.kitsune.app.ui.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kitsune.app.core.model.PlatformType
import com.kitsune.app.core.model.VideoQuality
import com.kitsune.app.ui.components.atoms.CobaltButton
import com.kitsune.app.ui.components.atoms.CobaltIconButton
import com.kitsune.app.ui.components.molecules.UrlInputBar
import com.kitsune.app.ui.theme.KitsuneTheme

@Composable
fun MainInputCard(
    url: String,
    onUrlChange: (String) -> Unit,
    detectedPlatform: PlatformType,
    selectedQuality: VideoQuality,
    audioOnly: Boolean,
    isDownloading: Boolean,
    onDownloadClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(KitsuneTheme.shapes.cardRadius)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(KitsuneTheme.colors.surface)
            .border(1.dp, KitsuneTheme.colors.borderSubtle, shape)
            .padding(KitsuneTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(KitsuneTheme.spacing.md)
    ) {
        UrlInputBar(
            url = url,
            onUrlChange = onUrlChange,
            onDownloadClick = onDownloadClick
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KitsuneTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CobaltIconButton(
                onClick = onOpenSettingsClick,
                size = 48.dp,
                containerColor = KitsuneTheme.colors.surfaceVariant
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Configurações de Download",
                    tint = KitsuneTheme.colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            CobaltButton(
                onClick = onDownloadClick,
                modifier = Modifier.weight(1f),
                enabled = url.isNotBlank() && !isDownloading,
                loading = isDownloading
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(KitsuneTheme.spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    val label = if (audioOnly) "Baixar Áudio" else "Baixar (${selectedQuality.label.take(10)})"
                    Text(text = label)
                }
            }
        }
    }
}
