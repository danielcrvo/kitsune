package com.kitsune.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitsune.app.core.model.DownloadState
import com.kitsune.app.ui.components.atoms.MascotSvg
import com.kitsune.app.ui.components.atoms.MascotType
import com.kitsune.app.ui.components.molecules.KitsuneModeSelector
import com.kitsune.app.ui.components.molecules.MediaPreviewCard
import com.kitsune.app.ui.components.molecules.UrlInputBar
import com.kitsune.app.ui.components.organisms.ActiveDownloadCard
import com.kitsune.app.ui.components.organisms.DownloadSettingsSheet
import com.kitsune.app.ui.components.organisms.DownloadsHistorySheet
import com.kitsune.app.ui.components.organisms.KitsuneMediaPlayerDialog
import com.kitsune.app.ui.components.organisms.SupportedServicesDialog
import com.kitsune.app.ui.components.organisms.TermsDialog
import com.kitsune.app.ui.theme.KitsuneTheme
import com.kitsune.app.ui.theme.ThemePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    initialSharedUrl: String? = null,
    onClearSharedUrl: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.initEngineVersion(context)
    }

    LaunchedEffect(initialSharedUrl) {
        if (!initialSharedUrl.isNullOrBlank()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.onUrlChanged(initialSharedUrl)
            onClearSharedUrl()
        } else if (uiState.url.isBlank()) {
            viewModel.checkClipboardForMediaUrl(context)
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    LaunchedEffect(uiState.downloadState) {
        when (uiState.downloadState) {
            is DownloadState.Completed -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            is DownloadState.Error -> {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            else -> Unit
        }
    }

    MainScreenContent(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                is MainUiAction.StartDownload -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                is MainUiAction.CancelDownload,
                is MainUiAction.SetDownloadMode,
                is MainUiAction.ToggleSupportedServices,
                is MainUiAction.ToggleHistory,
                is MainUiAction.ToggleSettings,
                is MainUiAction.ToggleTerms,
                is MainUiAction.PlayFile,
                is MainUiAction.CloseMediaPlayer -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                else -> Unit
            }
            viewModel.onAction(action, context)
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    uiState: MainUiState,
    onAction: (MainUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KitsuneTheme.colors.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(CircleShape)
                        .background(KitsuneTheme.colors.surfaceVariant)
                        .border(BorderStroke(1.dp, KitsuneTheme.colors.borderSubtle), CircleShape)
                        .clickable(onClick = { onAction(MainUiAction.ToggleSupportedServices(true)) })
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "+",
                            color = KitsuneTheme.colors.accentPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "serviços suportados",
                            color = KitsuneTheme.colors.textSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(KitsuneTheme.colors.surfaceVariant)
                            .border(BorderStroke(1.dp, KitsuneTheme.colors.borderSubtle), CircleShape)
                            .clickable(onClick = { onAction(MainUiAction.ToggleHistory(true)) }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = "Downloads",
                            tint = KitsuneTheme.colors.textMuted,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(KitsuneTheme.colors.surfaceVariant)
                            .border(BorderStroke(1.dp, KitsuneTheme.colors.borderSubtle), CircleShape)
                            .clickable(onClick = { onAction(MainUiAction.ToggleSettings(true)) }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Configurações",
                            tint = KitsuneTheme.colors.textMuted,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        KitsuneTheme.colors.accentOrange.copy(alpha = 0.24f),
                                        KitsuneTheme.colors.accentOrange.copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    val currentMascot = when (uiState.downloadState) {
                        is DownloadState.Downloading, is DownloadState.Muxing -> MascotType.DOWNLOADING
                        is DownloadState.Completed -> MascotType.COMPLETED
                        is DownloadState.Error -> MascotType.ERROR
                        else -> MascotType.IDLE
                    }

                    MascotSvg(
                        type = currentMascot,
                        contentDescription = "Kitsune Mascote",
                        size = 165.dp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "kitsune.tools",
                    color = KitsuneTheme.colors.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "o downloader mais fofo e rápido da\nweb!",
                    color = KitsuneTheme.colors.textMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(34.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UrlInputBar(
                    url = uiState.url,
                    onUrlChange = { onAction(MainUiAction.ChangeUrl(it)) },
                    onDownloadClick = { onAction(MainUiAction.StartDownload) }
                )

                if (uiState.detectedClipboardUrl != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KitsuneTheme.colors.surfaceVariant)
                            .border(BorderStroke(1.dp, KitsuneTheme.colors.borderSubtle), RoundedCornerShape(12.dp))
                            .clickable { onAction(MainUiAction.ChangeUrl(uiState.detectedClipboardUrl)) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentPaste,
                                    contentDescription = null,
                                    tint = KitsuneTheme.colors.accentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Copiar link detectado?",
                                    color = KitsuneTheme.colors.textSecondary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "Colar",
                                color = KitsuneTheme.colors.accentCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                MediaPreviewCard(
                    mediaInfo = uiState.mediaInfo,
                    isLoading = uiState.isLoadingMetadata,
                    modifier = Modifier.fillMaxWidth()
                )

                KitsuneModeSelector(
                    selectedMode = uiState.downloadMode,
                    onModeSelect = { onAction(MainUiAction.SetDownloadMode(it)) }
                )
            }

            if (uiState.downloadState !is DownloadState.Idle) {
                Spacer(modifier = Modifier.height(20.dp))
                ActiveDownloadCard(
                    downloadState = uiState.downloadState,
                    onCancel = { onAction(MainUiAction.CancelDownload) },
                    onDismissError = { onAction(MainUiAction.DismissError) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(48.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "ao continuar, você concorda com os",
                    color = KitsuneTheme.colors.textMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "termos e ética de uso",
                    color = KitsuneTheme.colors.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = { onAction(MainUiAction.ToggleTerms(true)) })
                )
            }
        }

        if (uiState.isSupportedServicesOpen) {
            SupportedServicesDialog(onDismiss = { onAction(MainUiAction.ToggleSupportedServices(false)) })
        }

        if (uiState.isHistoryOpen) {
            DownloadsHistorySheet(
                files = uiState.downloadedFiles,
                isLoading = uiState.isLoadingDownloadedFiles,
                onPlayFile = { onAction(MainUiAction.PlayFile(it)) },
                onDeleteFile = { onAction(MainUiAction.DeleteFile(it)) },
                onRenameFile = { file, newName -> onAction(MainUiAction.RenameFile(file, newName)) },
                onDismiss = { onAction(MainUiAction.ToggleHistory(false)) }
            )
        }

        if (uiState.isSettingsSheetOpen) {
            DownloadSettingsSheet(
                config = uiState.downloadConfig,
                engineVersion = uiState.engineVersion,
                isAmoledTheme = uiState.isAmoledTheme,
                onConfigChange = { onAction(MainUiAction.ChangeConfig(it)) },
                onToggleAmoledTheme = { onAction(MainUiAction.ToggleAmoledTheme(it)) },
                onCheckEngineUpdate = { onAction(MainUiAction.CheckEngineUpdate) },
                onDismiss = { onAction(MainUiAction.ToggleSettings(false)) }
            )
        }

        if (uiState.isTermsOpen) {
            TermsDialog(onDismiss = { onAction(MainUiAction.ToggleTerms(false)) })
        }

        uiState.playingFile?.let { file ->
            KitsuneMediaPlayerDialog(
                file = file,
                onDismiss = { onAction(MainUiAction.CloseMediaPlayer) },
                onOpenExternal = { onAction(MainUiAction.PlayExternal(file)) }
            )
        }
    }
}

@ThemePreviews
@Composable
private fun MainScreenPreview() {
    KitsuneTheme {
        MainScreenContent(
            uiState = MainUiState(
                url = "",
                isUrlValid = false
            ),
            onAction = {}
        )
    }
}
