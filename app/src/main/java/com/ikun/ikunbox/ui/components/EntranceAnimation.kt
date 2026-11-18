package com.ikun.ikunbox.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

private val EntranceEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

private const val EntranceDurationMs = 480

private const val EntranceOffsetPx = 40f

private const val EntranceMinScale = 0.9f

fun Modifier.animateItemEntrance(
    entered: Boolean,
    delayMillis: Int = 0,
): Modifier = composed {
    val progress by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(
            durationMillis = EntranceDurationMs,
            delayMillis = delayMillis,
            easing = EntranceEasing,
        ),
        label = "itemEntrance",
    )
    Modifier.graphicsLayer {
        val eased = progress
        translationY = (1f - eased) * EntranceOffsetPx * density
        scaleX = EntranceMinScale + (1f - EntranceMinScale) * eased
        scaleY = scaleX
        alpha = eased
    }
}
