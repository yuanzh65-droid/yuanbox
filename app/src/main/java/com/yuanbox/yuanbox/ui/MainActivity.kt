package com.yuanbox.yuanbox.ui

import android.content.res.Configuration
import android.os.Build
import com.yuanbox.yuanbox.ui.theme.enableTransparentEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import com.yuanbox.yuanbox.R
import com.yuanbox.yuanbox.ui.base.BaseActivity
import com.yuanbox.yuanbox.player.PlaybackService
import com.yuanbox.yuanbox.ui.page.MainScreen
import com.yuanbox.yuanbox.ui.theme.AVBoxTheme
import com.yuanbox.yuanbox.ui.theme.AppThemeState
import com.yuanbox.yuanbox.util.PermissionHelper

private const val PREWARM_ON_RESUME_DELAY_MS = 2000L

class MainActivity : BaseActivity() {

    override fun shouldRefreshAutoSize(): Boolean = true

    override fun hideSysBar() {
    }

    private fun applyStatusBarAppearance() {
        val night = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val light = !AppThemeState.isDark(night)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = light
            isAppearanceLightNavigationBars = light
        }
    }

    override fun onResume() {
        super.onResume()
        applyStatusBarAppearance()
        PlaybackService.prewarm(this, PREWARM_ON_RESUME_DELAY_MS)
    }

    override fun init() {
        enableTransparentEdgeToEdge()
        applyStatusBarAppearance()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !PermissionHelper.isStorageGranted(this)) {
            PermissionHelper.requestStorage(this) { _, _ -> }
        }
        PermissionHelper.requestNotificationIfNeeded(this)
        findViewById<ComposeView>(R.id.compose_view).setContent {
            AVBoxTheme {
                MainScreen()
            }
        }
    }
}
