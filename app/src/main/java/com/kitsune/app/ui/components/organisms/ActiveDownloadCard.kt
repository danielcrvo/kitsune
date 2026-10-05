package com.kitsune.app.ui.components.organisms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kitsune.app.core.model.DownloadState
import com.kitsune.app.ui.components.atoms.KitsuneIconButton
import com.kitsune.app.ui.components.molecules.DownloadStatusDisplay
import com.kitsune.app.ui.theme.KitsuneTheme

@Composable
fun ActiveDownloadCard(
    downloadState: DownloadState,
    onCancel: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = downloadState !is DownloadState.Idle
    val shape = RoundedCornerShape(KitsuneTheme.shapes.cardRadius)

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(KitsuneTheme.colors.surface)
                .border(1.dp, KitsuneTheme.colors.borderSubtle, shape)
                .padding(KitsuneTheme.spacing.lg)
        ) {
            when (downloadState) {
                is DownloadState.Downloading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(KitsuneTheme.spacing.sm)
                        ) {
                            com.kitsune.app.ui.components.atoms.MascotSvg(
                                type = com.kitsune.app.ui.components.atoms.MascotType.DOWNLOADING,
                                contentDescription = "Mascote Baixando",
                                size = 48.dp
                            )
                            Text(
                                text = "Download Ativo",
                                style = KitsuneTheme.typography.titleMedium,
                                color = KitsuneTheme.colors.textPrimary
                            )
                        }
                        KitsuneIconButton(
                            onClick = onCancel,
                            size = 32.dp,
                            containerColor = KitsuneTheme.colors.surfaceElevated
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar Download",
                                tint = KitsuneTheme.colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DownloadStatusDisplay(
                        progress = downloadState.progress,
                        stage = downloadState.stage,
                        speed = downloadState.speed,
                        eta = downloadState.eta,
                        modifier = Modifier.padding(top = KitsuneTheme.spacing.md)
                    )
                }

                is DownloadState.Muxing -> {
                    Text(
                        text = "Unindo Áudio e Vídeo...",
                        style = KitsuneTheme.typography.titleMedium,
                        color = KitsuneTheme.colors.accentCyan
                    )
                    Text(
                        text = "Processando arquivo final em alta resolução com FFmpeg local.",
                        style = KitsuneTheme.typography.bodyMedium,
                        color = KitsuneTheme.colors.textSecondary,
                        modifier = Modifier.padding(top = KitsuneTheme.spacing.xs)
                    )
                }

                is DownloadState.Completed -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        com.kitsune.app.ui.components.atoms.MascotSvg(
                            type = com.kitsune.app.ui.components.atoms.MascotType.COMPLETED,
                            contentDescription = "Mascote Comemorando",
                            size = 64.dp
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = KitsuneTheme.spacing.md)
                        ) {
                            Text(
                                text = "Salvo na Galeria!",
                                style = KitsuneTheme.typography.titleMedium,
                                color = KitsuneTheme.colors.success
                            )
                            Text(
                                text = "${downloadState.title} (${downloadState.fileSizeFormatted})",
                                style = KitsuneTheme.typography.labelSmall,
                                color = KitsuneTheme.colors.textMuted
                            )
                        }
                    }
                }

                is DownloadState.Error -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        com.kitsune.app.ui.components.atoms.MascotSvg(
                            type = com.kitsune.app.ui.components.atoms.MascotType.ERROR,
                            contentDescription = "Mascote com Erro",
                            size = 64.dp
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = KitsuneTheme.spacing.md)
                        ) {
                            Text(
                                text = "Falha no Download",
                                style = KitsuneTheme.typography.titleMedium,
                                color = KitsuneTheme.colors.error
                            )
                            Text(
                                text = downloadState.message,
                                style = KitsuneTheme.typography.bodyMedium,
                                color = KitsuneTheme.colors.textSecondary
                            )
                        }
                        KitsuneIconButton(
                            onClick = onDismissError,
                            size = 32.dp,
                            containerColor = KitsuneTheme.colors.surfaceElevated
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar erro",
                                tint = KitsuneTheme.colors.textSecondary
                            )
                        }
                    }
                }

                else -> {}
            }
        }
    }
}
