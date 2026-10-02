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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitsune.app.core.model.DownloadConfig
import com.kitsune.app.core.model.DownloadedMediaFile
import com.kitsune.app.core.model.DownloadState
import com.kitsune.app.ui.components.atoms.MascotSvg
import com.kitsune.app.ui.components.atoms.MascotType
import com.kitsune.app.ui.components.molecules.CobaltModeSelector
import com.kitsune.app.ui.components.molecules.UrlInputBar
import com.kitsune.app.ui.components.organisms.ActiveDownloadCard
import com.kitsune.app.ui.components.organisms.DownloadSettingsSheet
import com.kitsune.app.ui.components.organisms.DownloadsHistorySheet
import com.kitsune.app.ui.components.organisms.SupportedServicesDialog
import com.kitsune.app.ui.components.organisms.TermsDialog
import com.kitsune.app.ui.theme.KitsuneTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    initialSharedUrl: String? = null
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.initEngineVersion(context)
        if (!initialSharedUrl.isNullOrBlank()) {
            viewModel.onUrlChanged(initialSharedUrl)
        } else {
            viewModel.checkClipboardForMediaUrl(context)
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    MainScreenContent(
        uiState = uiState,
        onUrlChange = viewModel::onUrlChanged,
        onDownloadClick = { viewModel.startDownload(context) },
        onCancelDownload = { viewModel.cancelDownload(context) },
        onDismissError = viewModel::dismissError,
        onModeSelect = viewModel::setDownloadMode,
        onOpenSupportedServices = { viewModel.toggleSupportedServices(true) },
        onDismissSupportedServices = { viewModel.toggleSupportedServices(false) },
        onOpenHistory = { viewModel.toggleHistory(true) },
        onDismissHistory = { viewModel.toggleHistory(false) },
        onOpenSettings = { viewModel.toggleSettingsSheet(true) },
        onDismissSettings = { viewModel.toggleSettingsSheet(false) },
        onOpenTerms = { viewModel.toggleTerms(true) },
        onDismissTerms = { viewModel.toggleTerms(false) },
        onConfigChange = viewModel::onConfigChanged,
        onCheckEngineUpdate = { viewModel.checkEngineUpdate(context) },
        onPlayFile = { viewModel.playDownloadedFile(context, it) },
        onDeleteFile = viewModel::confirmDeleteFile,
        onRenameFile = { file, newName -> viewModel.confirmRenameFile(file, newName) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    uiState: MainUiState,
    onUrlChange: (String) -> Unit,
    onDownloadClick: () -> Unit,
    onCancelDownload: () -> Unit,
    onDismissError: () -> Unit,
    onModeSelect: (DownloadMode) -> Unit,
    onOpenSupportedServices: () -> Unit,
    onDismissSupportedServices: () -> Unit,
    onOpenHistory: () -> Unit,
    onDismissHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissSettings: () -> Unit,
    onOpenTerms: () -> Unit,
    onDismissTerms: () -> Unit,
    onConfigChange: (DownloadConfig) -> Unit,
    onCheckEngineUpdate: () -> Unit,
    onPlayFile: (DownloadedMediaFile) -> Unit,
    onDeleteFile: (DownloadedMediaFile) -> Unit,
    onRenameFile: (DownloadedMediaFile, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
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
                        .background(Color(0xFF131622))
                        .border(BorderStroke(1.dp, Color(0xFF232738)), CircleShape)
                        .clickable(onClick = onOpenSupportedServices)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "+",
                            color = Color(0xFFF97316),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "serviços suportados",
                            color = Color(0xFFCBD5E1),
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
                            .background(Color(0xFF131622))
                            .border(BorderStroke(1.dp, Color(0xFF232738)), CircleShape)
                            .clickable(onClick = onOpenHistory),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = "Downloads",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(19.dp)
                        )
                    }


                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF131622))
                            .border(BorderStroke(1.dp, Color(0xFF232738)), CircleShape)
                            .clickable(onClick = onOpenSettings),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Configurações",
                            tint = Color(0xFF94A3B8),
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
                                        Color(0xFFE27C2A).copy(alpha = 0.24f),
                                        Color(0xFFE27C2A).copy(alpha = 0.08f),
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

                Spacer(modifier = Modifier.height(6.dp))


                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF221A14))
                        .border(BorderStroke(1.dp, Color(0xFF3F2E20)), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "turbo",
                            color = Color(0xFFD97706),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))


                Text(
                    text = "kitsune.tools",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))


                Text(
                    text = "o downloader mais fofo e rápido da\nweb!",
                    color = Color(0xFF94A3B8),
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
                    onUrlChange = onUrlChange,
                    onDownloadClick = onDownloadClick
                )


                if (uiState.detectedClipboardUrl != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(Color(0xFF161A29))
                            .border(BorderStroke(1.dp, Color(0xFF283048)), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .clickable { onUrlChange(uiState.detectedClipboardUrl) }
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
                                Text("📋", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Copiar link detectado?",
                                    color = Color(0xFFCBD5E1),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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


                com.kitsune.app.ui.components.molecules.MediaPreviewCard(
                    mediaInfo = uiState.mediaInfo,
                    isLoading = uiState.isLoadingMetadata,
                    modifier = Modifier.fillMaxWidth()
                )


                CobaltModeSelector(
                    selectedMode = uiState.downloadMode,
                    onModeSelect = onModeSelect
                )
            }


            if (uiState.downloadState !is DownloadState.Idle) {
                Spacer(modifier = Modifier.height(20.dp))
                ActiveDownloadCard(
                    downloadState = uiState.downloadState,
                    onCancel = onCancelDownload,
                    onDismissError = onDismissError,
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
                    color = Color(0xFF64748B),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "termos e ética de uso",
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = onOpenTerms)
                )
            }
        }


        if (uiState.isSupportedServicesOpen) {
            SupportedServicesDialog(onDismiss = onDismissSupportedServices)
        }

        if (uiState.isHistoryOpen) {
            DownloadsHistorySheet(
                files = uiState.downloadedFiles,
                isLoading = uiState.isLoadingDownloadedFiles,
                onPlayFile = onPlayFile,
                onDeleteFile = onDeleteFile,
                onRenameFile = onRenameFile,
                onDismiss = onDismissHistory
            )
        }

        if (uiState.isSettingsSheetOpen) {
            DownloadSettingsSheet(
                config = uiState.downloadConfig,
                engineVersion = uiState.engineVersion,
                onConfigChange = onConfigChange,
                onCheckEngineUpdate = onCheckEngineUpdate,
                onDismiss = onDismissSettings
            )
        }

        if (uiState.isTermsOpen) {
            TermsDialog(onDismiss = onDismissTerms)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF090A0F)
@Composable
fun MainScreenPreview() {
    KitsuneTheme {
        MainScreenContent(
            uiState = MainUiState(
                url = "",
                isUrlValid = false
            ),
            onUrlChange = {},
            onDownloadClick = {},
            onCancelDownload = {},
            onDismissError = {},
            onModeSelect = {},
            onOpenSupportedServices = {},
            onDismissSupportedServices = {},
            onOpenHistory = {},
            onDismissHistory = {},
            onOpenSettings = {},
            onDismissSettings = {},
            onOpenTerms = {},
            onDismissTerms = {},
            onConfigChange = {},
            onCheckEngineUpdate = {},
            onPlayFile = {},
            onDeleteFile = {},
            onRenameFile = { _, _ -> }
        )
    }
}
