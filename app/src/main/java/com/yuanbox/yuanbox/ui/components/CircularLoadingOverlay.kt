package com.yuanbox.yuanbox.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

private val OrbitDotColors = listOf(
    Color(0xFFF59EE0),
    Color(0xFF3EE6C0),
    Color(0xFFFFC94D),
    Color(0xFF59B6FF),
)

@Composable
fun CircularLoadingOverlay(
    modifier: Modifier = Modifier,
    dotRadius: Dp = 5.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val progress = rememberOrbitProgress()
    Box(modifier = modifier.clip(CircleShape)) {
        content()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    val radius = size.minDimension / 2f
                    val strokeWidth = radius * 0.055f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.92f),
                        radius = radius - strokeWidth / 2f,
                        style = Stroke(width = strokeWidth),
                    )
                    val dotRadiusPx = dotRadius.toPx()
                    OrbitDotColors.forEachIndexed { index, color ->
                        val angle = progress * 360f + index * (360f / OrbitDotColors.size)
                        val rad = Math.toRadians(angle.toDouble())
                        val cx = radius + (radius - strokeWidth) * cos(rad).toFloat()
                        val cy = radius + (radius - strokeWidth) * sin(rad).toFloat()
                        drawCircle(
                            color = color.copy(alpha = 0.28f),
                            radius = dotRadiusPx * 2.2f,
                            center = Offset(cx, cy),
                        )
                        drawCircle(
                            color = color,
                            radius = dotRadiusPx,
                            center = Offset(cx, cy),
                        )
                    }
                },
        )
    }
}

@Composable
private fun rememberOrbitProgress(): Float {
    val transition = rememberInfiniteTransition(label = "orbit")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbitProgress",
    )
    return progress
}
