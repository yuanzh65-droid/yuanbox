package com.ikun.ikunbox.data

import com.ikun.ikunbox.util.KVKeys
import com.ikun.ikunbox.util.HistoryHelper
import com.ikun.ikunbox.util.KV

internal object CurrentSubscription {

    fun cid(): String {
        val apiUrl = KV.get(KVKeys.API_URL, "")
        val lineSource = KV.get(KVKeys.API_LINE_SOURCE, "")
        if (lineSource != null && lineSource.isNotEmpty() && HistoryHelper.isApiLineUrl(apiUrl)) {
            return lineSource
        }
        return apiUrl
    }
}
