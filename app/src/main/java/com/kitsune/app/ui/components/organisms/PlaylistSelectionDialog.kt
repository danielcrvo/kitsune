package com.kitsune.app.ui.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kitsune.app.R
import com.kitsune.app.domain.model.PlaylistInfo
import com.kitsune.app.domain.model.PlaylistItem
import com.kitsune.app.ui.components.atoms.KitsuneButton
import com.kitsune.app.ui.components.atoms.MascotSvg
import com.kitsune.app.ui.components.atoms.MascotType
import com.kitsune.app.ui.theme.KitsuneTheme

@Composable
fun PlaylistSelectionDialog(
    playlistInfo: PlaylistInfo,
    selectedItemIds: Set<String>,
    onToggleItem: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onConfirmDownload: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = KitsuneTheme.colors.surfaceElevated,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MascotSvg(
                        type = MascotType.SURPRISED,
                        contentDescription = null,
                        size = 48.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.playlist_dialog_title),
                            color = KitsuneTheme.colors.accentCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = playlistInfo.title,
                            color = KitsuneTheme.colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.playlist_total_items, playlistInfo.items.size),
                            color = KitsuneTheme.colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onSelectAll) {
                        Text(
                            text = stringResource(R.string.playlist_select_all),
                            color = KitsuneTheme.colors.accentCyan,
                            fontSize = 13.sp
                        )
                    }

                    TextButton(onClick = onDeselectAll) {
                        Text(
                            text = stringResource(R.string.playlist_deselect_all),
                            color = KitsuneTheme.colors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(playlistInfo.items, key = { it.id }) { item ->
                        PlaylistItemRow(
                            item = item,
                            isSelected = selectedItemIds.contains(item.id),
                            onToggle = { onToggleItem(item.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.btn_cancel),
                            color = KitsuneTheme.colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    KitsuneButton(
                        onClick = onConfirmDownload,
                        enabled = selectedItemIds.isNotEmpty()
                    ) {
                        Text(
                            text = stringResource(R.string.playlist_download_selected, selectedItemIds.size)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistItemRow(
    item: PlaylistItem,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = KitsuneTheme.colors.accentCyan,
                uncheckedColor = KitsuneTheme.colors.textSecondary,
                checkmarkColor = KitsuneTheme.colors.background
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${item.index}. ${item.title}",
                color = KitsuneTheme.colors.textPrimary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val durationText = if (item.durationSeconds > 0) {
                val m = item.durationSeconds / 60
                val s = item.durationSeconds % 60
                "%02d:%02d".format(m, s)
            } else ""

            if (durationText.isNotEmpty()) {
                Text(
                    text = durationText,
                    color = KitsuneTheme.colors.textSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
