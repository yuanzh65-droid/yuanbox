package com.yuanbox.yuanbox.net

import com.yuanbox.yuanbox.bean.SourceBean
import com.yuanbox.yuanbox.util.KVKeys
import com.yuanbox.yuanbox.util.KV
import com.yuanbox.yuanbox.util.SearchSettingsState

object SearchHelper {

    @JvmStatic
    var liveSourcesProvider: (() -> List<SourceBean>?)? = null

    @JvmStatic
    fun getSourcesForSearch(): HashMap<String, String>? {
        var checkSources: HashMap<String, String>? = null
        try {
            val api = KV.get(KVKeys.API_URL, "")
            if (api.isEmpty()) return null
            checkSources = SearchSettingsState.config.sourcesForSearch[api]
        } catch (e: Exception) {
            return null
        }
        if (checkSources == null || checkSources.isEmpty()) {
            checkSources = getSources()
        }
        return checkSources
    }

    @JvmStatic
    fun isSelectionStale(checked: HashMap<String, String>?): Boolean {
        val liveKeys = HashSet<String>()
        for (bean in liveSourcesProvider?.invoke().orEmpty()) {
            val key = bean.key ?: continue
            liveKeys.add(key)
        }
        return isSelectionStale(checked, liveKeys)
    }

    @JvmStatic
    fun isSelectionStale(checked: HashMap<String, String>?, liveSourceKeys: Set<String>): Boolean {
        if (checked == null || checked.isEmpty()) return false
        for (checkedKey in checked.keys) {
            if (!liveSourceKeys.contains(checkedKey)) return true
        }
        return false
    }

    @JvmStatic
    fun getSources(): HashMap<String, String> {
        val checkSources = HashMap<String, String>()
        for (bean in liveSourcesProvider?.invoke().orEmpty()) {
            if (!bean.isSearchable()) {
                continue
            }
            val key = bean.key ?: continue
            checkSources[key] = "1"
        }
        return checkSources
    }

}
