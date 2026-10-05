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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                Text(
                    text = "serviços suportados",
                    color = KitsuneTheme.colors.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = "O Kitsune suporta download direto e privado das seguintes plataformas:",
                    color = KitsuneTheme.colors.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ServiceItem("YouTube", "4K (2160p), 60fps, Shorts e áudios")
                    ServiceItem("TikTok", "HD sem marca d'água e áudios")
                    ServiceItem("Instagram", "Reels, posts e vídeos")
                    ServiceItem("X (Twitter)", "Vídeos e GIFs em qualidade original")
                    ServiceItem("Reddit", "Vídeos com áudio mesclado localmente")
                    ServiceItem("Bilibili & Outros", "SoundCloud, Pinterest e centenas de sites")
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Todos os downloads ocorrem 100% no seu aparelho sem proxy ou servidores intermediários.",
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
                            text = "fechar",
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
