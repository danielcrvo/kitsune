package com.kitsune.app.ui.components.organisms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitsune.app.core.model.AudioCodec
import com.kitsune.app.core.model.AudioQuality
import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.VideoQuality
import com.kitsune.app.ui.components.atoms.CobaltButton
import com.kitsune.app.ui.components.atoms.CobaltButtonVariant
import com.kitsune.app.ui.components.atoms.CobaltIconButton
import com.kitsune.app.ui.components.molecules.QualityOptionTile
import com.kitsune.app.ui.theme.KitsuneTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadSettingsSheet(
    config: DownloadConfig,
    engineVersion: String,
    onConfigChange: (DownloadConfig) -> Unit,
    onCheckEngineUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

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
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
        ) {
            // Header fixo no topo com título, subtítulo e botão de fechar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KitsuneTheme.spacing.lg)
                    .padding(top = KitsuneTheme.spacing.md, bottom = KitsuneTheme.spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Opções de Download",
                        style = KitsuneTheme.typography.titleLarge,
                        color = KitsuneTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configurações de áudio, vídeo e engine",
                        style = KitsuneTheme.typography.labelSmall,
                        color = KitsuneTheme.colors.textMuted
                    )
                }
                CobaltIconButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        onDismiss()
                    },
                    containerColor = KitsuneTheme.colors.surfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = KitsuneTheme.colors.textPrimary
                    )
                }
            }

            // Corpo rolável suave com todas as opções
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = KitsuneTheme.spacing.lg)
                    .padding(bottom = KitsuneTheme.spacing.xl)
            ) {
                Spacer(modifier = Modifier.height(KitsuneTheme.spacing.sm))

                // Switch Apenas Áudio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = KitsuneTheme.spacing.md)
                    ) {
                        Text(
                            text = "Apenas Áudio",
                            style = KitsuneTheme.typography.titleMedium,
                            color = KitsuneTheme.colors.textPrimary
                        )
                        Text(
                            text = "Extrair apenas o som em MP3 ou formato selecionado",
                            style = KitsuneTheme.typography.labelSmall,
                            color = KitsuneTheme.colors.textMuted
                        )
                    }
                    Switch(
                        checked = config.audioOnly,
                        onCheckedChange = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            onConfigChange(config.copy(audioOnly = it))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = KitsuneTheme.colors.background,
                            checkedTrackColor = KitsuneTheme.colors.accentCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(KitsuneTheme.spacing.md))

                // Formato de Áudio
                Text(
                    text = "Formato de Áudio",
                    style = KitsuneTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = KitsuneTheme.colors.textPrimary,
                    modifier = Modifier.padding(bottom = KitsuneTheme.spacing.xs)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AudioCodec.entries.forEach { codec ->
                        val isSelected = config.audioCodec == codec
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFF97316).copy(alpha = 0.15f) else Color(0xFF131722))
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFFF97316) else Color(0xFF222738)
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    onConfigChange(config.copy(audioCodec = codec))
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = codec.name,
                                color = if (isSelected) Color(0xFFF97316) else Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(KitsuneTheme.spacing.md))

                // Qualidade do Áudio
                Text(
                    text = "Qualidade do Áudio",
                    style = KitsuneTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = KitsuneTheme.colors.textPrimary,
                    modifier = Modifier.padding(bottom = KitsuneTheme.spacing.xs)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AudioQuality.entries.forEach { quality ->
                        val isSelected = config.audioQuality == quality
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF00F2FE).copy(alpha = 0.12f) else Color(0xFF131722))
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF00F2FE) else Color(0xFF222738)
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    onConfigChange(config.copy(audioQuality = quality))
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF00F2FE) else Color(0xFF64748B),
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = quality.label,
                                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF00F2FE)
                                    )
                                }
                            }
                        }
                    }
                }

                if (!config.audioOnly) {
                    Spacer(modifier = Modifier.height(KitsuneTheme.spacing.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = KitsuneTheme.spacing.md)
                        ) {
                            Text(
                                text = "Embutir Legendas",
                                style = KitsuneTheme.typography.titleMedium,
                                color = KitsuneTheme.colors.textPrimary
                            )
                            Text(
                                text = "Salvar legendas disponíveis dentro do MP4",
                                style = KitsuneTheme.typography.labelSmall,
                                color = KitsuneTheme.colors.textMuted
                            )
                        }
                        Switch(
                            checked = config.embedSubtitles,
                            onCheckedChange = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                onConfigChange(config.copy(embedSubtitles = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = KitsuneTheme.colors.background,
                                checkedTrackColor = KitsuneTheme.colors.accentCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(KitsuneTheme.spacing.md))

                    Text(
                        text = "Resolução Máxima de Vídeo",
                        style = KitsuneTheme.typography.titleMedium,
                        color = KitsuneTheme.colors.textPrimary,
                        modifier = Modifier.padding(bottom = KitsuneTheme.spacing.sm)
                    )

                    val qualities = listOf(
                        VideoQuality.AUTO,
                        VideoQuality.Q_2160P,
                        VideoQuality.Q_1440P,
                        VideoQuality.Q_1080P,
                        VideoQuality.Q_720P
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(KitsuneTheme.spacing.xs)
                    ) {
                        qualities.forEach { quality ->
                            QualityOptionTile(
                                quality = quality,
                                isSelected = config.quality == quality,
                                onSelect = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    onConfigChange(config.copy(quality = quality))
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(KitsuneTheme.spacing.lg))

                // Engine Local
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = KitsuneTheme.spacing.sm)
                    ) {
                        Text(
                            text = "Engine Local (yt-dlp)",
                            style = KitsuneTheme.typography.bodyMedium,
                            color = KitsuneTheme.colors.textPrimary
                        )
                        Text(
                            text = "Versão: $engineVersion",
                            style = KitsuneTheme.typography.labelSmall,
                            color = KitsuneTheme.colors.accentCyan
                        )
                    }
                    CobaltButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            onCheckEngineUpdate()
                        },
                        variant = CobaltButtonVariant.SECONDARY
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = KitsuneTheme.colors.accentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Atualizar Engine", color = KitsuneTheme.colors.accentCyan)
                    }
                }

                Spacer(modifier = Modifier.height(KitsuneTheme.spacing.md))
            }
        }
    }
}
