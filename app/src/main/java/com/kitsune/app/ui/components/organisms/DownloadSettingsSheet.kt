package com.kitsune.app.ui.components.organisms

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.AppShortcut
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.VideoSettings
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitsune.app.BuildConfig
import com.kitsune.app.R
import com.kitsune.app.core.engine.AppUpdateManager
import com.kitsune.app.core.model.AudioCodec
import com.kitsune.app.core.model.AudioQuality
import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.VideoQuality
import com.kitsune.app.ui.components.atoms.KitsuneButton
import com.kitsune.app.ui.components.atoms.KitsuneButtonVariant
import com.kitsune.app.ui.components.atoms.KitsuneIconButton
import com.kitsune.app.ui.theme.KitsuneTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadSettingsSheet(
    config: DownloadConfig,
    engineVersion: String,
    appVersion: String = "",
    isCheckingAppUpdate: Boolean = false,
    isCheckingEngineUpdate: Boolean = false,
    isAmoledTheme: Boolean = false,
    isDynamicColor: Boolean = false,
    isWifiOnly: Boolean = false,
    isAutoCheckUpdates: Boolean = true,
    apkCacheSizeBytes: Long = 0L,
    onConfigChange: (DownloadConfig) -> Unit,
    onToggleAmoledTheme: (Boolean) -> Unit = {},
    onToggleDynamicColor: (Boolean) -> Unit = {},
    onToggleWifiOnly: (Boolean) -> Unit = {},
    onToggleAutoCheckUpdates: (Boolean) -> Unit = {},
    onCheckEngineUpdate: () -> Unit,
    onCheckAppUpdate: () -> Unit = {},
    onClearUpdateCache: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = KitsuneTheme.colors.surface,
        dragHandle = null,
        shape = RoundedCornerShape(
            topStart = KitsuneTheme.shapes.sheetRadius,
            topEnd = KitsuneTheme.shapes.sheetRadius
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = KitsuneTheme.typography.titleLarge,
                        color = KitsuneTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.settings_subtitle),
                        style = KitsuneTheme.typography.labelSmall,
                        color = KitsuneTheme.colors.textMuted
                    )
                }
                KitsuneIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDismiss()
                    },
                    containerColor = KitsuneTheme.colors.surfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.btn_close),
                        tint = KitsuneTheme.colors.textPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.DarkMode,
                        title = stringResource(R.string.settings_amoled_title),
                        subtitle = stringResource(R.string.settings_amoled_desc),
                        checked = isAmoledTheme,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleAmoledTheme(it)
                        }
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Palette,
                            title = stringResource(R.string.pref_dynamic_color),
                            subtitle = stringResource(R.string.pref_dynamic_color_desc),
                            checked = isDynamicColor,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleDynamicColor(it)
                            }
                        )
                    }
                }

                SettingsSection(title = stringResource(R.string.settings_section_downloads)) {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Wifi,
                        title = stringResource(R.string.pref_wifi_only),
                        subtitle = stringResource(R.string.pref_wifi_only_desc),
                        checked = isWifiOnly,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleWifiOnly(it)
                        }
                    )

                    SettingsDivider()

                    SettingsSwitchRow(
                        icon = Icons.Outlined.Subtitles,
                        title = stringResource(R.string.settings_embed_subtitles),
                        subtitle = stringResource(R.string.settings_embed_subtitles_desc),
                        checked = config.embedSubtitles,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onConfigChange(config.copy(embedSubtitles = it))
                        }
                    )

                    SettingsDivider()

                    SettingsSwitchRow(
                        icon = Icons.Outlined.MusicNote,
                        title = stringResource(R.string.settings_audio_only),
                        subtitle = stringResource(R.string.settings_audio_only_desc),
                        checked = config.audioOnly,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onConfigChange(config.copy(audioOnly = it))
                        }
                    )

                    AnimatedVisibility(
                        visible = config.audioOnly,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.settings_audio_format),
                                style = KitsuneTheme.typography.labelSmall,
                                color = KitsuneTheme.colors.textMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AudioCodec.entries.forEach { codec ->
                                    val isSelected = config.audioCodec == codec
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) KitsuneTheme.colors.accentOrange.copy(alpha = 0.2f)
                                                else KitsuneTheme.colors.surfaceVariant
                                            )
                                            .border(
                                                BorderStroke(
                                                    1.dp,
                                                    if (isSelected) KitsuneTheme.colors.accentOrange
                                                    else KitsuneTheme.colors.borderSubtle
                                                ),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onConfigChange(config.copy(audioCodec = codec))
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = codec.name,
                                            color = if (isSelected) KitsuneTheme.colors.accentOrange else KitsuneTheme.colors.textMuted,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Text(
                                text = stringResource(R.string.settings_audio_quality),
                                style = KitsuneTheme.typography.labelSmall,
                                color = KitsuneTheme.colors.textMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AudioQuality.entries.forEach { quality ->
                                    val isSelected = config.audioQuality == quality
                                    val shortLabel = when (quality) {
                                        AudioQuality.BEST -> "320k"
                                        AudioQuality.HIGH -> "256k"
                                        AudioQuality.MEDIUM -> "192k"
                                        AudioQuality.LOW -> "128k"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) KitsuneTheme.colors.accentCyan.copy(alpha = 0.2f)
                                                else KitsuneTheme.colors.surfaceVariant
                                            )
                                            .border(
                                                BorderStroke(
                                                    1.dp,
                                                    if (isSelected) KitsuneTheme.colors.accentCyan
                                                    else KitsuneTheme.colors.borderSubtle
                                                ),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onConfigChange(config.copy(audioQuality = quality))
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = shortLabel,
                                            color = if (isSelected) KitsuneTheme.colors.accentCyan else KitsuneTheme.colors.textMuted,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!config.audioOnly) {
                        SettingsDivider()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.VideoSettings,
                                    contentDescription = null,
                                    tint = KitsuneTheme.colors.accentCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = stringResource(R.string.settings_video_quality),
                                        style = KitsuneTheme.typography.titleMedium,
                                        color = KitsuneTheme.colors.textPrimary
                                    )
                                }
                            }

                            val qualities = listOf(
                                VideoQuality.AUTO,
                                VideoQuality.Q_2160P,
                                VideoQuality.Q_1080P,
                                VideoQuality.Q_720P
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                qualities.forEach { quality ->
                                    val isSelected = config.quality == quality
                                    val label = when (quality) {
                                        VideoQuality.AUTO -> "Auto"
                                        VideoQuality.Q_2160P -> "4K"
                                        VideoQuality.Q_1080P -> "1080p"
                                        VideoQuality.Q_720P -> "720p"
                                        else -> quality.name
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) KitsuneTheme.colors.accentCyan.copy(alpha = 0.2f)
                                                else KitsuneTheme.colors.surfaceVariant
                                            )
                                            .border(
                                                BorderStroke(
                                                    1.dp,
                                                    if (isSelected) KitsuneTheme.colors.accentCyan
                                                    else KitsuneTheme.colors.borderSubtle
                                                ),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onConfigChange(config.copy(quality = quality))
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) KitsuneTheme.colors.accentCyan else KitsuneTheme.colors.textMuted,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                SettingsSection(title = stringResource(R.string.settings_section_system)) {
                    if (BuildConfig.UPDATER_ENABLED) {
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Update,
                            title = stringResource(R.string.settings_auto_check_title),
                            subtitle = stringResource(R.string.settings_auto_check_desc),
                            checked = isAutoCheckUpdates,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleAutoCheckUpdates(it)
                            }
                        )

                        SettingsDivider()

                        SettingsActionRow(
                            icon = Icons.Outlined.AppShortcut,
                            title = stringResource(R.string.settings_app_version_title),
                            subtitle = stringResource(R.string.settings_app_version, appVersion),
                            buttonText = stringResource(R.string.btn_retry),
                            isLoading = isCheckingAppUpdate,
                            onAction = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onCheckAppUpdate()
                            }
                        )

                        SettingsDivider()
                    }

                    SettingsActionRow(
                        icon = Icons.Outlined.Terminal,
                        title = stringResource(R.string.settings_engine_title),
                        subtitle = stringResource(R.string.settings_engine_version, engineVersion),
                        buttonText = stringResource(R.string.settings_engine_update_check),
                        isLoading = isCheckingEngineUpdate,
                        onAction = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCheckEngineUpdate()
                        }
                    )

                    if (BuildConfig.UPDATER_ENABLED) {
                        SettingsDivider()

                        SettingsActionRow(
                            icon = Icons.Outlined.CleaningServices,
                            title = stringResource(R.string.settings_clear_cache_title),
                            subtitle = stringResource(
                                R.string.settings_clear_cache_desc,
                                AppUpdateManager.formatFileSize(apkCacheSizeBytes)
                            ),
                            buttonText = stringResource(R.string.settings_clear_cache_btn),
                            isButtonEnabled = apkCacheSizeBytes > 0,
                            onAction = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onClearUpdateCache()
                            }
                        )
                    }
                }

                SettingsSection(title = stringResource(R.string.settings_section_about)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenAbout()
                            }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = KitsuneTheme.colors.accentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.about_dialog_title),
                                    style = KitsuneTheme.typography.titleMedium,
                                    color = KitsuneTheme.colors.textPrimary
                                )
                                Text(
                                    text = stringResource(R.string.settings_about_summary, appVersion),
                                    style = KitsuneTheme.typography.labelSmall,
                                    color = KitsuneTheme.colors.textMuted
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = KitsuneTheme.colors.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = KitsuneTheme.typography.labelSmall,
            color = KitsuneTheme.colors.accentCyan,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(KitsuneTheme.colors.surfaceElevated)
                .border(
                    BorderStroke(1.dp, KitsuneTheme.colors.borderSubtle),
                    RoundedCornerShape(16.dp)
                ),
            content = content
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) KitsuneTheme.colors.accentCyan else KitsuneTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = KitsuneTheme.typography.titleMedium,
                    color = KitsuneTheme.colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = KitsuneTheme.typography.labelSmall,
                    color = KitsuneTheme.colors.textMuted
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = KitsuneTheme.colors.background,
                checkedTrackColor = KitsuneTheme.colors.accentCyan
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    buttonText: String,
    isLoading: Boolean = false,
    isButtonEnabled: Boolean = true,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = KitsuneTheme.colors.accentCyan,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = KitsuneTheme.typography.titleMedium,
                    color = KitsuneTheme.colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = KitsuneTheme.typography.labelSmall,
                    color = KitsuneTheme.colors.textMuted
                )
            }
        }
        KitsuneButton(
            onClick = onAction,
            enabled = isButtonEnabled,
            loading = isLoading,
            variant = KitsuneButtonVariant.SECONDARY
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                tint = if (isButtonEnabled) KitsuneTheme.colors.accentCyan else KitsuneTheme.colors.textMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = buttonText,
                color = if (isButtonEnabled) KitsuneTheme.colors.accentCyan else KitsuneTheme.colors.textMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(KitsuneTheme.colors.borderSubtle)
    )
}
