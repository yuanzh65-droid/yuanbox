@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.yuanbox.yuanbox.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yuanbox.yuanbox.R
import com.yuanbox.yuanbox.ui.base.BaseActivity
import com.yuanbox.yuanbox.ui.player.PlayContainer
import com.yuanbox.yuanbox.ui.theme.AVBoxTheme
import com.yuanbox.yuanbox.ui.theme.enableTransparentEdgeToEdge
import com.yuanbox.yuanbox.util.DanmuSettingsState
import kotlin.math.abs

/**
 * 短剧全屏竖屏播放器。
 *
 * 从短剧模式的卡片列表进入，支持：
 * - 竖屏全屏沉浸播放；
 * - 上滑切下一部、下滑切上一部；
 * - 打开即出画（预载详情 + 首集直链，移除加载动画与大封面）；
 * - 连续 3 秒无画面自动重连。
 *
 * 播放内核复用 [PlayContainer] / 全局播放引擎，进度自动写入历史并可续播。
 */
class ShortDramaPlayerActivity : BaseActivity() {

    private val vm: ShortDramaPlayerViewModel by viewModels()

    private var playContainer: PlayContainer? = null

    override fun init() {
        enableTransparentEdgeToEdge()
        vm.initFromIntent(intent)
        findViewById<androidx.compose.ui.platform.ComposeView>(R.id.compose_view).setContent {
            AVBoxTheme(manageStatusBarIcons = false) {
                ShortDramaPlayerScreen(
                    activity = this@ShortDramaPlayerActivity,
                    vm = vm,
                    ensureContainer = { ensurePlayContainer() },
                )
            }
        }
    }

    private fun ensurePlayContainer(): PlayContainer {
        if (playContainer == null) {
            playContainer = PlayContainer(this).also {
                it.pageHostRef = object : com.yuanbox.yuanbox.player.PageHost {
                    override fun context(): Context = this@ShortDramaPlayerActivity
                    override fun isPageAlive(): Boolean = !isFinishing && !isDestroyed
                    override fun runOnUi(action: Runnable) { if (!isFinishing) runOnUiThread(action) }
                    override fun toast(text: CharSequence) { Toast.makeText(this@ShortDramaPlayerActivity, text, Toast.LENGTH_SHORT).show() }
                    override fun launchLocalSubtitlePicker() = Unit
                    override fun requestNotificationPermission() = Unit
                    override fun showEpisodeSheet() = Unit
                    override fun onPlaybackLinesExhausted(): Boolean = false
                }
                it.setPreviewMode(false)
            }
        }
        return requireNotNull(playContainer)
    }

    override fun onPause() {
        playContainer?.hostPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        playContainer?.hostResume()
    }

    override fun onDestroy() {
        playContainer?.hostDestroy()
        playContainer = null
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_LIST = "short_drama_list"
        private const val EXTRA_INDEX = "short_drama_index"

        fun start(context: Context, videos: List<com.yuanbox.yuanbox.bean.Movie.Video>, index: Int) {
            if (videos.isEmpty()) return
            val ids = videos.mapNotNull { it.id }
            val sourceKey = videos.firstOrNull()?.sourceKey.orEmpty()
            val names = videos.map { it.name.orEmpty() }
            val pics = videos.map { it.pic.orEmpty() }
            val intent = Intent(context, ShortDramaPlayerActivity::class.java).apply {
                putStringArrayListExtra("sd_ids", ArrayList(ids))
                putExtra("sd_source", sourceKey)
                putStringArrayListExtra("sd_names", ArrayList(names))
                putStringArrayListExtra("sd_pics", ArrayList(pics))
                putExtra(EXTRA_INDEX, index.coerceIn(0, videos.size - 1))
            }
            context.startActivity(intent)
        }
    }
}

@Composable
private fun ShortDramaPlayerScreen(
    activity: Activity,
    vm: ShortDramaPlayerViewModel,
    ensureContainer: () -> PlayContainer,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 当有可播放会话时，绑定容器并播放
    val readySession = (state as? ShortDramaPlayerViewModel.PlayerState.Ready)?.session
    androidx.compose.runtime.LaunchedEffect(readySession) {
        if (readySession != null) {
            vm.bindContainer(ensureContainer(), readySession)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                var accumulated = 0f
                detectDragGestures(
                    onDragStart = { accumulated = 0f },
                    onDragEnd = {
                        val dy = accumulated
                        val threshold = 120f
                        if (abs(dy) > threshold) {
                            if (dy < 0) vm.next() else vm.previous()
                        }
                        accumulated = 0f
                    },
                    onDragCancel = { accumulated = 0f },
                ) { change, drag ->
                    change.consume()
                    accumulated += drag.y
                }
            },
    ) {
        when (val s = state) {
            is ShortDramaPlayerViewModel.PlayerState.Ready -> {
                ShortDramaVideoSurface(container = ensureContainer())
            }

            is ShortDramaPlayerViewModel.PlayerState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = context.getString(R.string.common_loading_more),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            is ShortDramaPlayerViewModel.PlayerState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = s.message,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        // 顶部标题
        val title = (state as? ShortDramaPlayerViewModel.PlayerState.Ready)?.title.orEmpty()
        if (title.isNotEmpty()) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp),
            )
        }

        // 左下角「弹」字方框开关（即时生效，与详情共用弹幕设置）
        if (state is ShortDramaPlayerViewModel.PlayerState.Ready) {
            val danmuOpen = DanmuSettingsState.config.open
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 24.dp)
                    .background(
                        color = if (danmuOpen) Color(0xCCFFFFFF) else Color(0x66000000),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clickable {
                        val target = !DanmuSettingsState.config.open
                        DanmuSettingsState.open = target
                        ensureContainer().applyDanmuSettings(true)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "弹",
                    color = if (danmuOpen) Color.Black else Color.White,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun ShortDramaVideoSurface(container: PlayContainer) {
    // PlayContainer 是原生 View，通过 AndroidView 挂载
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { container },
        modifier = Modifier.fillMaxSize(),
    )
}
