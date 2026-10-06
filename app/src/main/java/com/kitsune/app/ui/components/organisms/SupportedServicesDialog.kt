package com.kitsune.app.ui.components.organisms

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitsune.app.R
import com.kitsune.app.ui.components.atoms.MascotSvg
import com.kitsune.app.ui.components.atoms.MascotType
import com.kitsune.app.ui.theme.KitsuneTheme
import com.kitsune.app.ui.theme.ThemePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportedServicesDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(KitsuneTheme.shapes.cardRadius))
                .background(KitsuneTheme.colors.surface)
                .border(BorderStroke(1.dp, KitsuneTheme.colors.borderSubtle), RoundedCornerShape(KitsuneTheme.shapes.cardRadius))
                .padding(22.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MascotSvg(
                        type = MascotType.LOVE,
                        contentDescription = null,
                        size = 48.dp
                    )
                    Text(
                        text = stringResource(R.string.services_dialog_title),
                        color = KitsuneTheme.colors.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = stringResource(R.string.services_dialog_desc),
                    color = KitsuneTheme.colors.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ServiceItem("YouTube", stringResource(R.string.services_youtube_desc))
                    ServiceItem("TikTok", stringResource(R.string.services_tiktok_desc))
                    ServiceItem("Instagram", stringResource(R.string.services_instagram_desc))
                    ServiceItem("X (Twitter)", stringResource(R.string.services_twitter_desc))
                    ServiceItem("Reddit", stringResource(R.string.services_reddit_desc))
                    ServiceItem("SoundCloud", stringResource(R.string.services_soundcloud_desc))
                    ServiceItem("Bilibili", stringResource(R.string.services_bilibili_desc))
                    ServiceItem("Pinterest", stringResource(R.string.services_pinterest_desc))
                    ServiceItem(stringResource(R.string.services_generic_title), stringResource(R.string.services_generic_desc))
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(R.string.services_privacy_note),
                    color = KitsuneTheme.colors.textMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.btn_close),
                            color = KitsuneTheme.colors.accentOrange,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceItem(name: String, desc: String) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            color = KitsuneTheme.colors.accentOrange,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Text(
            text = "$name: ",
            color = KitsuneTheme.colors.textPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Text(
            text = desc,
            color = KitsuneTheme.colors.textMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@ThemePreviews
@Composable
private fun SupportedServicesDialogPreview() {
    KitsuneTheme {
        SupportedServicesDialog(onDismiss = {})
    }
}
