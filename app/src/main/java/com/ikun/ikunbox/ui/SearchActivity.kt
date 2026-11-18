package com.ikun.ikunbox.ui

import androidx.activity.compose.setContent
import com.ikun.ikunbox.R
import com.ikun.ikunbox.ui.base.BaseActivity
import com.ikun.ikunbox.ui.components.SheetHostScaffold
import com.ikun.ikunbox.ui.theme.AVBoxTheme
import com.ikun.ikunbox.ui.theme.enableTransparentEdgeToEdge

class SearchActivity : BaseActivity() {

    override fun shouldRefreshAutoSize(): Boolean = true

    override fun hideSysBar() {
    }

    override fun init() {
        enableTransparentEdgeToEdge()
        findViewById<androidx.compose.ui.platform.ComposeView>(R.id.compose_view).setContent {
            AVBoxTheme {
                SheetHostScaffold {
                    SearchScreen()
                }
            }
        }
    }
}
