package com.kitsune.app.ui.components.atoms

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

enum class MascotType(val assetPath: String) {
    IDLE("file:///android_asset/mascot/mascot_idle.svg"),
    DOWNLOADING("file:///android_asset/mascot/mascot_downloading.svg"),
    COMPLETED("file:///android_asset/mascot/mascot_completed.svg"),
    ERROR("file:///android_asset/mascot/mascot_error.svg")
}

@Composable
fun MascotSvg(
    type: MascotType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val context = LocalContext.current
    val imageRequest = remember(type) {
        ImageRequest.Builder(context)
            .data(type.assetPath)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build()
    }

    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = modifier.size(size)
    )
}
