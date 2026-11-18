package com.ikun.ikunbox.ui

import android.content.Context
import android.content.Intent
import com.ikun.ikunbox.ui.theme.enableTransparentEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import com.ikun.ikunbox.R
import com.ikun.ikunbox.ui.base.BaseActivity
import com.ikun.ikunbox.ui.components.SheetHostScaffold
import com.ikun.ikunbox.ui.page.PreferenceSettingsScreen
import com.ikun.ikunbox.ui.theme.AVBoxTheme

class PreferenceSettingsActivity : BaseActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, PreferenceSettingsActivity::class.java))
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
                    PreferenceSettingsScreen(onNavigateBack = { finish() })
                }
            }
        }
    }
}
