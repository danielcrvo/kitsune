package com.kitsune.app.ui.components.atoms

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

enum class MascotType(
    val assetPath: String,
    val startFrame: Int,
    val endFrame: Int
) {
    IDLE(
        assetPath = "file:///android_asset/kitsune_idle.svg",
        startFrame = 0,
        endFrame = 59
    ),
    DOWNLOADING(
        assetPath = "file:///android_asset/kitsune_downloading.svg",
        startFrame = 60,
        endFrame = 119
    ),
    COMPLETED(
        assetPath = "file:///android_asset/kitsune_completed.svg",
        startFrame = 120,
        endFrame = 179
    ),
    ERROR(
        assetPath = "file:///android_asset/kitsune_error.svg",
        startFrame = 180,
        endFrame = 239
    ),
    SLEEPING(
        assetPath = "file:///android_asset/kitsune_sleeping.svg",
        startFrame = 240,
        endFrame = 299
    ),
    WAVING(
        assetPath = "file:///android_asset/kitsune_waving.svg",
        startFrame = 300,
        endFrame = 359
    ),
    SEARCHING(
        assetPath = "file:///android_asset/kitsune_searching.svg",
        startFrame = 360,
        endFrame = 419
    ),
    PAUSED(
        assetPath = "file:///android_asset/kitsune_paused.svg",
        startFrame = 420,
        endFrame = 479
    ),
    WAITING_FOR_WIFI(
        assetPath = "file:///android_asset/kitsune_offline.svg",
        startFrame = 480,
        endFrame = 539
    ),
    OFFLINE(
        assetPath = "file:///android_asset/kitsune_offline.svg",
        startFrame = 480,
        endFrame = 539
    ),
    DANCING(
        assetPath = "file:///android_asset/kitsune_dancing.svg",
        startFrame = 540,
        endFrame = 599
    ),
    THINKING(
        assetPath = "file:///android_asset/kitsune_thinking.svg",
        startFrame = 600,
        endFrame = 659
    ),
    SURPRISED(
        assetPath = "file:///android_asset/kitsune_surprised.svg",
        startFrame = 660,
        endFrame = 719
    ),
    LOVE(
        assetPath = "file:///android_asset/kitsune_love.svg",
        startFrame = 720,
        endFrame = 779
    ),
    EATING(
        assetPath = "file:///android_asset/kitsune_eating.svg",
        startFrame = 780,
        endFrame = 839
    ),
    QUEUE(
        assetPath = "file:///android_asset/kitsune_queued.svg",
        startFrame = 840,
        endFrame = 899
    ),
    QUEUED(
        assetPath = "file:///android_asset/kitsune_queued.svg",
        startFrame = 840,
        endFrame = 899
    ),
    MUXING(
        assetPath = "file:///android_asset/kitsune_converting.svg",
        startFrame = 900,
        endFrame = 959
    ),
    CONVERTING(
        assetPath = "file:///android_asset/kitsune_converting.svg",
        startFrame = 900,
        endFrame = 959
    ),
    COOL(
        assetPath = "file:///android_asset/kitsune_cold.svg",
        startFrame = 960,
        endFrame = 1019
    ),
    COLD(
        assetPath = "file:///android_asset/kitsune_cold.svg",
        startFrame = 960,
        endFrame = 1019
    ),
    ROCKET(
        assetPath = "file:///android_asset/kitsune_rolling.svg",
        startFrame = 1020,
        endFrame = 1079
    ),
    ROLLING(
        assetPath = "file:///android_asset/kitsune_rolling.svg",
        startFrame = 1020,
        endFrame = 1079
    ),
    SINGING(
        assetPath = "file:///android_asset/kitsune_singing.svg",
        startFrame = 1140,
        endFrame = 1199
    )
}

@Composable
fun MascotStaticSvg(
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

@Composable
fun MascotAnimation(
    type: MascotType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val isInspection = LocalInspectionMode.current
    val compositionResult = rememberLottieComposition(
        LottieCompositionSpec.Asset("kitsune.json")
    )
    val composition = compositionResult.value

    if (isInspection || composition == null) {
        MascotStaticSvg(
            type = type,
            contentDescription = contentDescription,
            modifier = modifier,
            size = size
        )
    } else {
        val clipSpec = remember(type) {
            LottieClipSpec.Frame(min = type.startFrame, max = type.endFrame)
        }
        val progress by animateLottieCompositionAsState(
            composition = composition,
            clipSpec = clipSpec,
            iterations = LottieConstants.IterateForever,
            isPlaying = true
        )

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = modifier
                .size(size)
                .semantics {
                    if (contentDescription != null) {
                        this.contentDescription = contentDescription
                    }
                }
        )
    }
}

@Composable
fun MascotSvg(
    type: MascotType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    animate: Boolean = true
) {
    if (animate) {
        MascotAnimation(
            type = type,
            contentDescription = contentDescription,
            modifier = modifier,
            size = size
        )
    } else {
        MascotStaticSvg(
            type = type,
            contentDescription = contentDescription,
            modifier = modifier,
            size = size
        )
    }
}
