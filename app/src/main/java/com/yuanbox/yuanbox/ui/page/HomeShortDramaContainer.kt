package com.yuanbox.yuanbox.ui.page

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * 首页 ⇄ 短剧 模式容器。
 * 首页左侧边缘向右滑动进入短剧模式；短剧模式通过返回按钮返回首页。
 * 手势检测限制在屏幕左边缘窄条区域，避免与首页内容手势冲突。
 */
@Composable
fun HomeShortDramaContainer(
    vm: HomeViewModel,
    contentPadding: PaddingValues,
) {
    var showShortDrama by remember { mutableStateOf(false) }
    val edgeWidth = 24.dp

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = showShortDrama,
            transitionSpec = {
                val toShortDrama = targetState
                (
                    slideInHorizontally(tween(280)) { full ->
                        if (toShortDrama) full else -full
                    }
                    ).togetherWith(
                    slideOutHorizontally(tween(280)) { full ->
                        if (toShortDrama) -full else full
                    }
                )
            },
            label = "homeShortDrama",
            modifier = Modifier.fillMaxSize(),
        ) { shortDrama ->
            if (shortDrama) {
                ShortDramaScreen(
                    vm = vm,
                    contentPadding = contentPadding,
                    onBack = { showShortDrama = false },
                )
            } else {
                HomePage(vm, contentPadding)
            }
        }

        // 左边缘手势条：仅在首页可见时生效
        if (!showShortDrama) {
            var accumulatedDx by remember { mutableFloatStateOf(0f) }
            val triggerThresholdPx = with(LocalDensity.current) { 60.dp.toPx() }
            Box(
                modifier = Modifier
                    .width(edgeWidth)
                    .fillMaxHeight()
                    .pointerInput(triggerThresholdPx) {
                        detectHorizontalDragGestures(
                            onDragStart = { accumulatedDx = 0f },
                            onDragEnd = {
                                if (accumulatedDx > triggerThresholdPx) {
                                    showShortDrama = true
                                }
                                accumulatedDx = 0f
                            },
                            onDragCancel = { accumulatedDx = 0f },
                        ) { _, dragAmount ->
                            accumulatedDx += dragAmount
                        }
                    },
            )
        }
    }
}
