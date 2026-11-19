package com.yuanbox.yuanbox.ui

import androidx.activity.compose.setContent
import com.yuanbox.yuanbox.R
import com.yuanbox.yuanbox.ui.base.BaseActivity
import com.yuanbox.yuanbox.ui.components.SheetHostScaffold
import com.yuanbox.yuanbox.ui.theme.AVBoxTheme
import com.yuanbox.yuanbox.ui.theme.enableTransparentEdgeToEdge

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
