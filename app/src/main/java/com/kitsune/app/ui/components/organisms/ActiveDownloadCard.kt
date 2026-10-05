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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kitsune.app.R
import com.kitsune.app.core.model.DownloadState
import com.kitsune.app.core.model.DownloadTask
import com.kitsune.app.ui.components.atoms.KitsuneIconButton
import com.kitsune.app.ui.components.atoms.MascotSvg
import com.kitsune.app.ui.components.atoms.MascotType
import com.kitsune.app.ui.components.molecules.DownloadStatusDisplay
import com.kitsune.app.ui.theme.KitsuneTheme
import com.kitsune.app.ui.theme.ThemePreviews

@Composable
fun ActiveDownloadCard(
    downloadState: DownloadState,
    onCancel: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
    queue: List<DownloadTask> = emptyList(),
    onCancelTask: (String) -> Unit = {}
) {
    val pendingTasks = queue.filter { it.state is DownloadState.Idle }
    val isVisible = downloadState !is DownloadState.Idle || pendingTasks.isNotEmpty()
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
                            MascotSvg(
                                type = MascotType.DOWNLOADING,
                                contentDescription = stringResource(R.string.cd_mascot_downloading),
                                size = 48.dp
                            )
                            Text(
                                text = stringResource(R.string.status_download_active),
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
                                contentDescription = stringResource(R.string.btn_cancel),
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

                is DownloadState.WaitingForWifi -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(KitsuneTheme.spacing.sm)
                        ) {
                            MascotSvg(
                                type = MascotType.WAITING_FOR_WIFI,
                                contentDescription = stringResource(R.string.status_waiting_wifi),
                                size = 48.dp
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.status_waiting_wifi),
                                    style = KitsuneTheme.typography.titleMedium,
                                    color = KitsuneTheme.colors.accentOrange
                                )
                                Text(
                                    text = stringResource(R.string.pref_wifi_only_desc),
                                    style = KitsuneTheme.typography.bodySmall,
                                    color = KitsuneTheme.colors.textSecondary
                                )
                            }
                        }
                        KitsuneIconButton(
                            onClick = onCancel,
                            size = 32.dp,
                            containerColor = KitsuneTheme.colors.surfaceElevated
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.btn_cancel),
                                tint = KitsuneTheme.colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                is DownloadState.Muxing -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(KitsuneTheme.spacing.sm)
                    ) {
                        MascotSvg(
                            type = MascotType.MUXING,
                            contentDescription = stringResource(R.string.status_muxing_title),
                            size = 48.dp
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.status_muxing_title),
                                style = KitsuneTheme.typography.titleMedium,
                                color = KitsuneTheme.colors.accentCyan
                            )
                            Text(
                                text = stringResource(R.string.status_muxing_desc),
                                style = KitsuneTheme.typography.bodyMedium,
                                color = KitsuneTheme.colors.textSecondary,
                                modifier = Modifier.padding(top = KitsuneTheme.spacing.xs)
                            )
                        }
                    }
                }

                is DownloadState.Completed -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MascotSvg(
                            type = MascotType.COMPLETED,
                            contentDescription = stringResource(R.string.cd_mascot_completed),
                            size = 64.dp
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = KitsuneTheme.spacing.md)
                        ) {
                            Text(
                                text = stringResource(R.string.status_saved_gallery),
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
                        MascotSvg(
                            type = MascotType.ERROR,
                            contentDescription = stringResource(R.string.cd_mascot_error),
                            size = 64.dp
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = KitsuneTheme.spacing.md)
                        ) {
                            Text(
                                text = stringResource(R.string.status_download_failed),
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
                                contentDescription = stringResource(R.string.btn_close),
                                tint = KitsuneTheme.colors.textSecondary
                            )
                        }
                    }
                }

                else -> {}
            }

            if (pendingTasks.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = KitsuneTheme.spacing.md),
                    color = KitsuneTheme.colors.borderSubtle
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(KitsuneTheme.spacing.xs)
                    ) {
                        MascotSvg(
                            type = MascotType.QUEUE,
                            contentDescription = stringResource(R.string.queue_title),
                            size = 28.dp
                        )
                        Text(
                            text = stringResource(R.string.queue_title),
                            style = KitsuneTheme.typography.labelLarge,
                            color = KitsuneTheme.colors.textPrimary
                        )
                    }
                    Text(
                        text = stringResource(R.string.queue_pending_count, pendingTasks.size),
                        style = KitsuneTheme.typography.labelSmall,
                        color = KitsuneTheme.colors.textMuted
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = KitsuneTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(KitsuneTheme.spacing.xs)
                ) {
                    pendingTasks.take(5).forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(KitsuneTheme.shapes.smallRadius))
                                .background(KitsuneTheme.colors.surfaceElevated)
                                .padding(horizontal = KitsuneTheme.spacing.sm, vertical = KitsuneTheme.spacing.xs),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = task.title.ifBlank { task.url },
                                style = KitsuneTheme.typography.bodySmall,
                                color = KitsuneTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = KitsuneTheme.spacing.sm)
                            )
                            KitsuneIconButton(
                                onClick = { onCancelTask(task.id) },
                                size = 24.dp,
                                containerColor = KitsuneTheme.colors.surface
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.queue_item_cancel),
                                    tint = KitsuneTheme.colors.textMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun ActiveDownloadCardPreview() {
    KitsuneTheme {
        ActiveDownloadCard(
            downloadState = DownloadState.Downloading(
                progress = 68f,
                speed = "4.2 MB/s",
                eta = "00:15",
                stage = com.kitsune.app.core.model.DownloadStage.VIDEO_STREAM
            ),
            onCancel = {},
            onDismissError = {}
        )
    }
}


