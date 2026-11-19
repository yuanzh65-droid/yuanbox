package com.yuanbox.yuanbox.ui

import android.content.Context
import android.content.Intent
import com.yuanbox.yuanbox.ui.theme.enableTransparentEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import com.yuanbox.yuanbox.R
import com.yuanbox.yuanbox.ui.base.BaseActivity
import com.yuanbox.yuanbox.ui.components.SheetHostScaffold
import com.yuanbox.yuanbox.ui.page.ThemeSettingsScreen
import com.yuanbox.yuanbox.ui.theme.AVBoxTheme

class ThemeSettingsActivity : BaseActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ThemeSettingsActivity::class.java))
        }
    }

    override fun shouldRefreshAutoSize(): Boolean = true

    override fun hideSysBar() {
    }

    override fun init() {
        enableTransparentEdgeToEdge()
        findViewById<ComposeView>(R.id.compose_view).setContent {
            AVBoxTheme {
                SheetHostScaffold {
                    ThemeSettingsScreen(onNavigateBack = { finish() })
                }
            }
        }
    }
}
