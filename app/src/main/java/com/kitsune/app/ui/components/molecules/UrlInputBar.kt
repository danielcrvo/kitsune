package com.kitsune.app.ui.components.molecules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitsune.app.ui.theme.KitsuneTheme

@Composable
fun UrlInputBar(
    url: String,
    onUrlChange: (String) -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "cole o link aqui"
) {
    val clipboardManager = LocalClipboardManager.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(KitsuneTheme.colors.surface)
            .border(BorderStroke(1.dp, KitsuneTheme.colors.borderSubtle), RoundedCornerShape(27.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = KitsuneTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))


            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (url.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = KitsuneTheme.colors.textMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }

                BasicTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = KitsuneTheme.colors.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(KitsuneTheme.colors.accentPrimary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onDownloadClick()
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))


            AnimatedVisibility(
                visible = url.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(KitsuneTheme.colors.surfaceVariant)
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            onUrlChange("")
                        }
                        .padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpar",
                        tint = KitsuneTheme.colors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))


            val isActionDownload = url.isNotBlank()

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(KitsuneTheme.colors.surfaceVariant)
                    .border(BorderStroke(1.dp, KitsuneTheme.colors.borderSubtle), RoundedCornerShape(14.dp))
                    .clickable {
                        if (isActionDownload) {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onDownloadClick()
                        } else {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            val clipText = clipboardManager.getText()?.text
                            if (!clipText.isNullOrBlank()) {
                                onUrlChange(clipText.trim())
                            }
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isActionDownload) Icons.Outlined.Download else Icons.Outlined.ContentPaste,
                        contentDescription = null,
                        tint = KitsuneTheme.colors.accentPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isActionDownload) "Baixar" else "Colar",
                        color = KitsuneTheme.colors.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@com.kitsune.app.ui.theme.ThemePreviews
@Composable
private fun UrlInputBarPreview() {
    KitsuneTheme {
        UrlInputBar(
            url = "",
            onUrlChange = {},
            onDownloadClick = {}
        )
    }
}

