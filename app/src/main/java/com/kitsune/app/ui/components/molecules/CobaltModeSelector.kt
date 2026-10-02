package com.kitsune.app.ui.components.molecules

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitsune.app.ui.screens.DownloadMode

@Composable
fun CobaltModeSelector(
    selectedMode: DownloadMode,
    onModeSelect: (DownloadMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFF0F121C))
            .border(BorderStroke(1.dp, Color(0xFF222738)), RoundedCornerShape(26.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeSegment(
                label = "auto",
                icon = Icons.Default.Star,
                activeColor = Color(0xFFF97316),
                isSelected = selectedMode == DownloadMode.AUTO,
                onClick = { onModeSelect(DownloadMode.AUTO) },
                modifier = Modifier.weight(1f)
            )

            ModeSegment(
                label = "áudio",
                icon = Icons.Default.MusicNote,
                activeColor = Color(0xFFC084FC),
                isSelected = selectedMode == DownloadMode.AUDIO,
                onClick = { onModeSelect(DownloadMode.AUDIO) },
                modifier = Modifier.weight(1f)
            )

            ModeSegment(
                label = "mudo",
                icon = Icons.AutoMirrored.Filled.VolumeOff,
                activeColor = Color(0xFFF87171),
                isSelected = selectedMode == DownloadMode.MUTE,
                onClick = { onModeSelect(DownloadMode.MUTE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeSegment(
    label: String,
    icon: ImageVector,
    activeColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgAnim by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFF1F5F9) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "segmentBg"
    )

    val textColorAnim by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF090A0F) else Color(0xFF94A3B8),
        animationSpec = tween(durationMillis = 200),
        label = "segmentText"
    )

    val iconColorAnim by animateColorAsState(
        targetValue = if (isSelected) activeColor else activeColor.copy(alpha = 0.85f),
        animationSpec = tween(durationMillis = 200),
        label = "segmentIcon"
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(bgAnim)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColorAnim,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = textColorAnim,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}
