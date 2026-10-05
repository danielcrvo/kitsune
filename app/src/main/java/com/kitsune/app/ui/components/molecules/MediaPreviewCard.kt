package com.kitsune.app.ui.components.molecules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kitsune.app.core.model.MediaInfo
import com.kitsune.app.ui.components.atoms.PlatformBadge
import com.kitsune.app.ui.theme.KitsuneTheme

@Composable
fun MediaPreviewCard(
    mediaInfo: MediaInfo?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isLoading || mediaInfo != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(KitsuneTheme.colors.surface)
                .border(BorderStroke(1.dp, KitsuneTheme.colors.borderSubtle), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            if (isLoading && mediaInfo == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = KitsuneTheme.colors.accentCyan
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "detectando detalhes da mídia...",
                        color = KitsuneTheme.colors.textMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            } else if (mediaInfo != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (!mediaInfo.thumbnailUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = mediaInfo.thumbnailUrl,
                            contentDescription = mediaInfo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 84.dp, height = 56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KitsuneTheme.colors.surfaceVariant)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(width = 84.dp, height = 56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KitsuneTheme.colors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "▶",
                                color = KitsuneTheme.colors.textMuted,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = mediaInfo.title,
                            color = KitsuneTheme.colors.textPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PlatformBadge(platform = mediaInfo.platform)

                            if (mediaInfo.durationSeconds > 0) {
                                val minutes = mediaInfo.durationSeconds / 60
                                val seconds = mediaInfo.durationSeconds % 60
                                val formattedDuration = "%02d:%02d".format(minutes, seconds)
                                Text(
                                    text = formattedDuration,
                                    color = KitsuneTheme.colors.textSecondary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }

                            if (mediaInfo.uploader.isNotBlank() && mediaInfo.uploader != "Desconhecido") {
                                Text(
                                    text = "• ${mediaInfo.uploader}",
                                    color = KitsuneTheme.colors.textMuted,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
