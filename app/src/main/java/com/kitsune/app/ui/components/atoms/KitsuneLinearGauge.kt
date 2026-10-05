package com.kitsune.app.ui.components.atoms

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kitsune.app.ui.theme.KitsuneTheme

@Composable
fun KitsuneLinearGauge(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 200),
        label = "gauge-progress"
    )

    val shape = RoundedCornerShape(KitsuneTheme.shapes.pill)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(KitsuneTheme.colors.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(shape)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            KitsuneTheme.colors.accentIndigo,
                            KitsuneTheme.colors.accentCyan
                        )
                    )
                )
        )
    }
}

@Preview(name = "KitsuneLinearGauge Preview")
@Composable
private fun KitsuneLinearGaugePreview() {
    KitsuneTheme {
        KitsuneLinearGauge(progress = 0.65f)
    }
}
