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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitsune.app.core.model.DownloadedMediaFile
import com.kitsune.app.ui.components.atoms.CobaltIconButton
import com.kitsune.app.ui.components.atoms.CobaltTextField
import com.kitsune.app.ui.components.atoms.MascotSvg
import com.kitsune.app.ui.components.atoms.MascotType
import com.kitsune.app.ui.theme.KitsuneTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class MediaFilter {
    ALL,
    VIDEOS,
    AUDIOS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsHistorySheet(
    files: List<DownloadedMediaFile>,
    isLoading: Boolean,
    onPlayFile: (DownloadedMediaFile) -> Unit,
    onDeleteFile: (DownloadedMediaFile) -> Unit,
    onRenameFile: (DownloadedMediaFile, String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var selectedFilter by remember { mutableStateOf(MediaFilter.ALL) }
    var fileToRename by remember { mutableStateOf<DownloadedMediaFile?>(null) }
    var fileToDelete by remember { mutableStateOf<DownloadedMediaFile?>(null) }
    var renameInputText by remember { mutableStateOf("") }

    val filteredFiles = remember(files, selectedFilter) {
        when (selectedFilter) {
            MediaFilter.ALL -> files
            MediaFilter.VIDEOS -> files.filter { it.isVideo }
            MediaFilter.AUDIOS -> files.filter { !it.isVideo }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F121C),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Arquivos Baixados",
                        style = KitsuneTheme.typography.titleLarge,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${files.size} itens salvos no dispositivo",
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }

                CobaltIconButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        onDismiss()
                    },
                    containerColor = Color(0xFF1E2435)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(
                    label = "Todos (${files.size})",
                    isSelected = selectedFilter == MediaFilter.ALL,
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        selectedFilter = MediaFilter.ALL
                    }
                )
                FilterChipItem(
                    label = "Vídeos (${files.count { it.isVideo }})",
                    isSelected = selectedFilter == MediaFilter.VIDEOS,
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        selectedFilter = MediaFilter.VIDEOS
                    }
                )
                FilterChipItem(
                    label = "Áudios (${files.count { !it.isVideo }})",
                    isSelected = selectedFilter == MediaFilter.AUDIOS,
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        selectedFilter = MediaFilter.AUDIOS
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFF97316))
                }
            } else if (filteredFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MascotSvg(
                            type = MascotType.IDLE,
                            contentDescription = null,
                            size = 80.dp
                        )
                        Text(
                            text = "Nenhum arquivo encontrado",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Os vídeos e áudios que você baixar aparecerão aqui.",
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredFiles, key = { it.id.toString() + it.fileName }) { item ->
                        DownloadedFileCard(
                            file = item,
                            onPlay = { onPlayFile(item) },
                            onRename = {
                                fileToRename = item
                                renameInputText = item.title
                            },
                            onDelete = { fileToDelete = item }
                        )
                    }
                }
            }
        }
    }

    fileToRename?.let { item ->
        AlertDialog(
            onDismissRequest = { fileToRename = null },
            containerColor = Color(0xFF131722),
            title = {
                Text(
                    text = "Renomear Arquivo",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Digite o novo nome para a mídia:",
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                    CobaltTextField(
                        value = renameInputText,
                        onValueChange = { renameInputText = it },
                        placeholder = "Nome do arquivo",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        val newName = renameInputText.trim()
                        if (newName.isNotBlank()) {
                            onRenameFile(item, newName)
                        }
                        fileToRename = null
                    }
                ) {
                    Text(
                        text = "Salvar",
                        color = Color(0xFF00F2FE),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRename = null }) {
                    Text(
                        text = "Cancelar",
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        )
    }

    fileToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            containerColor = Color(0xFF131722),
            title = {
                Text(
                    text = "Excluir Arquivo",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Tem certeza de que deseja remover permanentemente \"${item.fileName}\" do dispositivo?",
                    color = Color(0xFFCBD5E1),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onDeleteFile(item)
                        fileToDelete = null
                    }
                ) {
                    Text(
                        text = "Excluir",
                        color = Color(0xFFFF4500),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text(
                        text = "Cancelar",
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        )
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFF97316) else Color(0xFF161A26))
            .border(
                BorderStroke(1.dp, if (isSelected) Color(0xFFF97316) else Color(0xFF222738)),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun DownloadedFileCard(
    file: DownloadedMediaFile,
    onPlay: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val dateString = remember(file.dateModified) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(file.dateModified))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131722))
            .border(BorderStroke(1.dp, Color(0xFF202638)), RoundedCornerShape(16.dp))
            .clickable {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onPlay()
            }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (file.isVideo) Color(0xFFF97316).copy(alpha = 0.18f) else Color(0xFF00F2FE).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (file.isVideo) Icons.Default.Movie else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (file.isVideo) Color(0xFFF97316) else Color(0xFF00F2FE),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.title,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = file.sizeFormatted,
                        color = Color(0xFF00F2FE),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "•",
                        color = Color(0xFF475569),
                        fontSize = 11.sp
                    )
                    Text(
                        text = dateString,
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CobaltIconButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        onPlay()
                    },
                    containerColor = Color(0xFF1E2435),
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Reproduzir",
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(16.dp)
                    )
                }

                CobaltIconButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        onRename()
                    },
                    containerColor = Color(0xFF1E2435),
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Renomear",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                }

                CobaltIconButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        onDelete()
                    },
                    containerColor = Color(0xFF1E2435),
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
