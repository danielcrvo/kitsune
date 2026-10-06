package com.kitsune.app.ui.components.organisms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitsune.app.R
import com.kitsune.app.core.engine.AppUpdateManager
import com.kitsune.app.core.model.AppUpdateInfo
import com.kitsune.app.core.model.AppUpdateState
import com.kitsune.app.ui.components.atoms.KitsuneButton
import com.kitsune.app.ui.components.atoms.KitsuneButtonVariant
import com.kitsune.app.ui.components.atoms.KitsuneLinearGauge
import com.kitsune.app.ui.components.atoms.MascotSvg
import com.kitsune.app.ui.components.atoms.MascotType
import com.kitsune.app.ui.theme.KitsuneTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateDialog(
    updateState: AppUpdateState,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val info: AppUpdateInfo? = when (updateState) {
        is AppUpdateState.UpdateAvailable -> updateState.info
        is AppUpdateState.Downloading -> updateState.info
        is AppUpdateState.ReadyToInstall -> updateState.info
        else -> null
    }

    if (info == null && updateState !is AppUpdateState.Error) {
        return
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(KitsuneTheme.shapes.cardRadius))
                .background(KitsuneTheme.colors.surface)
                .border(
                    BorderStroke(1.dp, KitsuneTheme.colors.borderSubtle),
                    RoundedCornerShape(KitsuneTheme.shapes.cardRadius)
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val mascotType = when (updateState) {
                    is AppUpdateState.ReadyToInstall -> MascotType.COMPLETED
                    is AppUpdateState.Downloading -> MascotType.ROCKET
                    is AppUpdateState.Error -> MascotType.ERROR
                    else -> MascotType.SURPRISED
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MascotSvg(
                        type = mascotType,
                        contentDescription = null,
                        size = 56.dp
                    )
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.update_dialog_title),
                            color = KitsuneTheme.colors.textPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        if (info != null) {
                            Text(
                                text = stringResource(R.string.update_dialog_version, info.versionName),
                                color = KitsuneTheme.colors.accentCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            if (info.fileSizeBytes > 0) {
                                Text(
                                    text = stringResource(R.string.update_dialog_size, AppUpdateManager.formatFileSize(info.fileSizeBytes)),
                                    color = KitsuneTheme.colors.textMuted,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                if (info != null && info.releaseNotes.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KitsuneTheme.colors.surfaceElevated)
                            .border(BorderStroke(1.dp, KitsuneTheme.colors.borderSubtle), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.update_dialog_changelog),
                            color = KitsuneTheme.colors.textSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = info.releaseNotes.trim(),
                                color = KitsuneTheme.colors.textMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                when (updateState) {
                    is AppUpdateState.Downloading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            KitsuneLinearGauge(
                                progress = (updateState.progressPercent / 100f).coerceIn(0f, 1f),
                                height = 8.dp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.update_downloading, updateState.progressPercent),
                                    color = KitsuneTheme.colors.accentCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${AppUpdateManager.formatFileSize(updateState.bytesDownloaded)} / ${AppUpdateManager.formatFileSize(updateState.totalBytes)}",
                                    color = KitsuneTheme.colors.textMuted,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    is AppUpdateState.ReadyToInstall -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(KitsuneTheme.colors.accentCyan.copy(alpha = 0.12f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = KitsuneTheme.colors.accentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.update_ready_to_install),
                                color = KitsuneTheme.colors.accentCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text(
                                    text = stringResource(R.string.update_dialog_btn_later),
                                    color = KitsuneTheme.colors.textMuted,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            KitsuneButton(
                                onClick = onInstall,
                                variant = KitsuneButtonVariant.PRIMARY
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.update_dialog_btn_install),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    is AppUpdateState.UpdateAvailable -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text(
                                    text = stringResource(R.string.update_dialog_btn_later),
                                    color = KitsuneTheme.colors.textMuted,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            KitsuneButton(
                                onClick = onDownload,
                                variant = KitsuneButtonVariant.PRIMARY
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.update_dialog_btn_download),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    is AppUpdateState.Error -> {
                        Text(
                            text = stringResource(R.string.update_download_failed, updateState.message),
                            color = KitsuneTheme.colors.accentOrange,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text(
                                    text = stringResource(R.string.btn_close),
                                    color = KitsuneTheme.colors.textMuted,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                            }
                            if (info != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                KitsuneButton(
                                    onClick = onDownload,
                                    variant = KitsuneButtonVariant.PRIMARY
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_retry),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}
