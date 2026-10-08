package com.kitsune.app.ui.main

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.merge
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.kitsune.app.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitsune.app.domain.model.DownloadState
import com.kitsune.app.ui.download.DownloadUiAction
import com.kitsune.app.ui.download.DownloadUiState
import com.kitsune.app.ui.download.DownloadViewModel
import com.kitsune.app.ui.history.HistoryUiAction
import com.kitsune.app.ui.history.HistoryUiState
import com.kitsune.app.ui.history.HistoryViewModel
import com.kitsune.app.ui.settings.SettingsUiAction
import com.kitsune.app.ui.settings.SettingsUiState
import com.kitsune.app.ui.settings.SettingsViewModel
import com.kitsune.app.ui.update.AppUpdateUiAction
import com.kitsune.app.ui.update.AppUpdateUiState
import com.kitsune.app.ui.update.AppUpdateViewModel
import com.kitsune.app.ui.components.atoms.MascotSvg
import com.kitsune.app.ui.components.atoms.MascotType
import com.kitsune.app.ui.components.molecules.KitsuneModeSelector
import com.kitsune.app.ui.components.molecules.MediaPreviewCard
import com.kitsune.app.ui.components.molecules.UrlInputBar
import com.kitsune.app.ui.components.organisms.AboutDialog
import com.kitsune.app.ui.components.organisms.ActiveDownloadCard
import com.kitsune.app.ui.components.organisms.AppUpdateDialog
import com.kitsune.app.ui.components.organisms.DownloadSettingsSheet
import com.kitsune.app.ui.components.organisms.DownloadsHistorySheet
import com.kitsune.app.ui.components.organisms.KitsuneMediaPlayerDialog
import com.kitsune.app.ui.components.organisms.PlaylistSelectionDialog
import com.kitsune.app.ui.components.organisms.SupportedServicesDialog
import com.kitsune.app.ui.components.organisms.TermsDialog
import com.kitsune.app.ui.theme.KitsuneTheme
import com.kitsune.app.ui.theme.ThemePreviews

@Composable
fun MainScreen(
    settingsViewModel: SettingsViewModel,
    initialSharedUrl: String?,
    onClearSharedUrl: () -> Unit,
    modifier: Modifier = Modifier,
    downloadViewModel: DownloadViewModel = viewModel(),
    historyViewModel: HistoryViewModel = viewModel(),
    appUpdateViewModel: AppUpdateViewModel = viewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val downloadState by downloadViewModel.uiState.collectAsStateWithLifecycle()
    val historyState by historyViewModel.uiState.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val appUpdateState by appUpdateViewModel.uiState.collectAsStateWithLifecycle()
    var overlay by rememberSaveable { mutableStateOf(MainOverlay.NONE) }

    LaunchedEffect(initialSharedUrl) {
        if (!initialSharedUrl.isNullOrBlank()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            downloadViewModel.onAction(DownloadUiAction.ChangeUrl(initialSharedUrl))
            onClearSharedUrl()
        } else if (downloadState.url.isBlank()) {
            downloadViewModel.onAction(DownloadUiAction.ClipboardTextAvailable(readClipboardText(context)))
        }
    }

    LaunchedEffect(Unit) {
        merge(historyViewModel.messages, settingsViewModel.messages, appUpdateViewModel.messages)
            .collect { message ->
                Toast.makeText(context, message.asString(context), Toast.LENGTH_SHORT).show()
            }
    }

    LaunchedEffect(downloadState.downloadState) {
        when (downloadState.downloadState) {
            is DownloadState.Completed -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            is DownloadState.Error -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            else -> Unit
        }
    }

    MainScreenContent(
        downloadState = downloadState,
        historyState = historyState,
        settingsState = settingsState,
        appUpdateState = appUpdateState,
        overlay = overlay,
        onAction = { action ->
            when (action) {
                DownloadUiAction.StartDownload -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                DownloadUiAction.CancelDownload,
                is DownloadUiAction.SetDownloadMode,
                is ShowOverlay,
                is HistoryUiAction.PlayFile,
                HistoryUiAction.CloseMediaPlayer -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                else -> Unit
            }
            when (action) {
                is ShowOverlay -> {
                    overlay = action.overlay
                    when (action.overlay) {
                        MainOverlay.HISTORY -> historyViewModel.onAction(HistoryUiAction.Refresh)
                        MainOverlay.SETTINGS -> settingsViewModel.onAction(SettingsUiAction.RefreshUpdateCache)
                        else -> Unit
                    }
                }
                is DownloadUiAction -> downloadViewModel.onAction(action)
                is HistoryUiAction -> historyViewModel.onAction(action)
                is SettingsUiAction -> settingsViewModel.onAction(action)
                is AppUpdateUiAction -> appUpdateViewModel.onAction(action)
            }
        },
        modifier = modifier
    )
}

private fun readClipboardText(context: Context): String? = runCatching {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = clipboard?.primaryClip
    if (clip == null || clip.itemCount == 0) null else clip.getItemAt(0).text?.toString()
}.getOrNull()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    downloadState: DownloadUiState,
    historyState: HistoryUiState,
    settingsState: SettingsUiState,
    appUpdateState: AppUpdateUiState,
    overlay: MainOverlay,
    onAction: (MainUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val closeOverlay = { onAction(ShowOverlay(MainOverlay.NONE)) }
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current
    var isInteractingWithMascot by remember { mutableStateOf(false) }

    LaunchedEffect(isInteractingWithMascot) {
        if (isInteractingWithMascot) {
            delay(2000L)
            isInteractingWithMascot = false
        }
    }

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
                        .clickable(onClick = { onAction(ShowOverlay(MainOverlay.SUPPORTED_SERVICES)) })
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
                            text = stringResource(R.string.services_dialog_title),
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
                            .clickable(onClick = { onAction(ShowOverlay(MainOverlay.HISTORY)) }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = stringResource(R.string.cd_open_history),
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
                            .clickable(onClick = { onAction(ShowOverlay(MainOverlay.SETTINGS)) }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.cd_open_settings),
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

                    val currentMascot = when {
                        isInteractingWithMascot -> MascotType.WAVING
                        downloadState.isResolvingLink -> MascotType.SEARCHING
                        settingsState.isCheckingEngineUpdate -> MascotType.ROCKET
                        downloadState.downloadState is DownloadState.Downloading -> {
                            if (downloadState.downloadConfig.audioOnly) MascotType.DANCING else MascotType.DOWNLOADING
                        }
                        downloadState.downloadState is DownloadState.Muxing -> MascotType.MUXING
                        downloadState.downloadState is DownloadState.WaitingForWifi -> MascotType.WAITING_FOR_WIFI
                        downloadState.downloadState is DownloadState.Completed -> MascotType.COMPLETED
                        downloadState.downloadState is DownloadState.Error -> MascotType.ERROR
                        else -> MascotType.IDLE
                    }

                    MascotSvg(
                        type = currentMascot,
                        contentDescription = stringResource(R.string.cd_mascot_idle),
                        size = 165.dp,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (downloadState.downloadState is DownloadState.Idle && !downloadState.isLoadingMetadata) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isInteractingWithMascot = true
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.app_name),
                    color = KitsuneTheme.colors.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.app_tagline),
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
                    url = downloadState.url,
                    onUrlChange = { onAction(DownloadUiAction.ChangeUrl(it)) },
                    onDownloadClick = { onAction(DownloadUiAction.StartDownload) }
                )

                val clipboardUrl = downloadState.detectedClipboardUrl
                if (clipboardUrl != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KitsuneTheme.colors.surfaceVariant)
                            .border(BorderStroke(1.dp, KitsuneTheme.colors.borderSubtle), RoundedCornerShape(12.dp))
                            .clickable { onAction(DownloadUiAction.ChangeUrl(clipboardUrl)) }
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
                                    text = stringResource(R.string.prompt_clipboard_detected),
                                    color = KitsuneTheme.colors.textSecondary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = stringResource(R.string.btn_paste),
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
                    mediaInfo = downloadState.mediaInfo,
                    isLoading = downloadState.isResolvingLink,
                    loadingText = if (downloadState.isLoadingPlaylist) {
                        stringResource(R.string.playlist_loading)
                    } else {
                        stringResource(R.string.preview_detecting)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                KitsuneModeSelector(
                    selectedMode = downloadState.downloadMode,
                    onModeSelect = { onAction(DownloadUiAction.SetDownloadMode(it)) }
                )
            }

            if (downloadState.hasActiveOrQueuedDownloads) {
                Spacer(modifier = Modifier.height(20.dp))
                ActiveDownloadCard(
                    downloadState = downloadState.downloadState,
                    queue = downloadState.downloadQueue,
                    onCancel = { onAction(DownloadUiAction.CancelDownload) },
                    onCancelTask = { onAction(DownloadUiAction.CancelQueueTask(it)) },
                    onDismissError = { onAction(DownloadUiAction.DismissError) },
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
                    text = stringResource(R.string.terms_agreement_prefix),
                    color = KitsuneTheme.colors.textMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.terms_dialog_title),
                    color = KitsuneTheme.colors.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = { onAction(ShowOverlay(MainOverlay.TERMS)) })
                )
            }
        }

        when (overlay) {
            MainOverlay.SUPPORTED_SERVICES -> SupportedServicesDialog(onDismiss = closeOverlay)
            MainOverlay.HISTORY -> DownloadsHistorySheet(
                files = historyState.files,
                isLoading = historyState.isLoading,
                onPlayFile = { onAction(HistoryUiAction.PlayFile(it)) },
                onDeleteFile = { onAction(HistoryUiAction.DeleteFile(it)) },
                onRenameFile = { file, newName -> onAction(HistoryUiAction.RenameFile(file, newName)) },
                onDismiss = closeOverlay
            )
            MainOverlay.SETTINGS -> DownloadSettingsSheet(
                config = downloadState.downloadConfig,
                engineVersion = settingsState.engineVersion,
                appVersion = settingsState.appVersionName,
                isCheckingAppUpdate = appUpdateState.isChecking,
                isCheckingEngineUpdate = settingsState.isCheckingEngineUpdate,
                isAmoledTheme = settingsState.isAmoledTheme,
                isDynamicColor = settingsState.isDynamicColor,
                isWifiOnly = settingsState.isWifiOnly,
                isAutoCheckUpdates = settingsState.isAutoCheckUpdates,
                isUpdaterEnabled = settingsState.isUpdaterEnabled,
                apkCacheSizeBytes = settingsState.apkCacheSizeBytes,
                onConfigChange = { onAction(DownloadUiAction.ChangeConfig(it)) },
                onToggleAmoledTheme = { onAction(SettingsUiAction.ToggleAmoledTheme(it)) },
                onToggleDynamicColor = { onAction(SettingsUiAction.ToggleDynamicColor(it)) },
                onToggleWifiOnly = { onAction(SettingsUiAction.ToggleWifiOnly(it)) },
                onToggleAutoCheckUpdates = { onAction(SettingsUiAction.ToggleAutoCheckUpdates(it)) },
                onCheckEngineUpdate = { onAction(SettingsUiAction.CheckEngineUpdate) },
                onCheckAppUpdate = { onAction(AppUpdateUiAction.CheckForUpdate) },
                onClearUpdateCache = { onAction(SettingsUiAction.ClearUpdateCache) },
                onOpenAbout = { onAction(ShowOverlay(MainOverlay.ABOUT)) },
                onDismiss = closeOverlay
            )
            MainOverlay.ABOUT -> AboutDialog(
                appVersion = settingsState.appVersionName,
                onOpenTerms = { onAction(ShowOverlay(MainOverlay.TERMS)) },
                onOpenSupportedServices = { onAction(ShowOverlay(MainOverlay.SUPPORTED_SERVICES)) },
                onDismiss = closeOverlay
            )
            MainOverlay.TERMS -> TermsDialog(onDismiss = closeOverlay)
            MainOverlay.NONE -> Unit
        }

        if (appUpdateState.isDialogOpen) {
            AppUpdateDialog(
                updateState = appUpdateState.updateState,
                onDownload = { onAction(AppUpdateUiAction.DownloadUpdate) },
                onInstall = { onAction(AppUpdateUiAction.InstallUpdate) },
                onDismiss = { onAction(AppUpdateUiAction.DismissDialog) }
            )
        }

        historyState.playingFile?.let { file ->
            KitsuneMediaPlayerDialog(
                file = file,
                onDismiss = { onAction(HistoryUiAction.CloseMediaPlayer) },
                onOpenExternal = { onAction(HistoryUiAction.PlayExternal(file)) }
            )
        }

        val playlistInfo = downloadState.playlistInfo
        if (downloadState.isPlaylistDialogOpen && playlistInfo != null) {
            PlaylistSelectionDialog(
                playlistInfo = playlistInfo,
                selectedItemIds = downloadState.selectedPlaylistItems,
                onToggleItem = { onAction(DownloadUiAction.TogglePlaylistItem(it)) },
                onSelectAll = { onAction(DownloadUiAction.SelectAllPlaylistItems) },
                onDeselectAll = { onAction(DownloadUiAction.DeselectAllPlaylistItems) },
                onConfirmDownload = { onAction(DownloadUiAction.DownloadSelectedPlaylistItems) },
                onDismiss = { onAction(DownloadUiAction.ClosePlaylistDialog) }
            )
        }
    }
}

@ThemePreviews
@Composable
private fun MainScreenPreview() {
    KitsuneTheme {
        MainScreenContent(
            downloadState = DownloadUiState(),
            historyState = HistoryUiState(),
            settingsState = SettingsUiState(),
            appUpdateState = AppUpdateUiState(),
            overlay = MainOverlay.NONE,
            onAction = {}
        )
    }
}
