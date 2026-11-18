package com.ikun.ikunbox.ui

import android.content.Context
import android.content.Intent
import com.ikun.ikunbox.ui.theme.enableTransparentEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import com.ikun.ikunbox.R
import com.ikun.ikunbox.ui.base.BaseActivity
import com.ikun.ikunbox.ui.page.PlaySettingsScreen
import com.ikun.ikunbox.ui.theme.AVBoxTheme

class PlaySettingsActivity : BaseActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, PlaySettingsActivity::class.java))
        }
    }

    override fun shouldRefreshAutoSize(): Boolean = true

    override fun hideSysBar() {
    }

    override fun init() {
        enableTransparentEdgeToEdge()
        findViewById<ComposeView>(R.id.compose_view).setContent {
            AVBoxTheme {
                PlaySettingsScreen(onNavigateBack = { finish() })
            }
        }
    }
}
