package com.yuanbox.yuanbox.data

import com.yuanbox.yuanbox.util.KVKeys
import com.yuanbox.yuanbox.util.HistoryHelper
import com.yuanbox.yuanbox.util.KV

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
