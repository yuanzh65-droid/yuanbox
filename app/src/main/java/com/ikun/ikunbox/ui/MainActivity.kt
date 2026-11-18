package com.ikun.ikunbox.ui

import android.content.res.Configuration
import android.os.Build
import com.ikun.ikunbox.ui.theme.enableTransparentEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import com.ikun.ikunbox.R
import com.ikun.ikunbox.ui.base.BaseActivity
import com.ikun.ikunbox.player.PlaybackService
import com.ikun.ikunbox.ui.page.MainScreen
import com.ikun.ikunbox.ui.theme.AVBoxTheme
import com.ikun.ikunbox.ui.theme.AppThemeState
import com.ikun.ikunbox.util.PermissionHelper

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
