package com.yuanbox.yuanbox.util

import com.yuanbox.yuanbox.api.ApiConfig
import com.yuanbox.yuanbox.bean.SourceBean

/**
 * 短剧模式的独立数据源设置。
 *
 * 与首页站点（[ApiConfig.homeSourceBean]）解耦：短剧源单独持久化，切换首页站点不会影响短剧源；
 * 仅当切换订阅（订阅 URL 变化）导致短剧源失效时，才需要重选。
 *
 * 存储的是源的 key（[SourceBean.key]），读取时通过 [ApiConfig.getSource] 解析回 [SourceBean]。
 */
object ShortDramaSettings {

    /**
     * 当前短剧源。若未设置或已失效，回退到首页站点。
     */
    fun currentSource(): SourceBean {
        val key = KV.get(KVKeys.SHORT_DRAMA_API, "")
        val resolved = ApiConfig.get().getSource(key)
        return resolved ?: ApiConfig.get().homeSourceBean()
    }

    /**
     * 是否已显式指定短剧源。
     */
    fun hasExplicitSource(): Boolean {
        val key = KV.get(KVKeys.SHORT_DRAMA_API, "")
        return key.isNotEmpty() && ApiConfig.get().getSource(key) != null
    }

    /**
     * 短剧源是否跟随订阅绑定：订阅切换后若源失效，需要重选。
     * 这里用「源 key 在订阅列表中仍然存在」来判定是否仍有效。
     */
    fun currentSourceValid(): Boolean {
        val key = KV.get(KVKeys.SHORT_DRAMA_API, "")
        if (key.isEmpty()) return true
        return ApiConfig.get().getSource(key) != null
    }

    fun setSource(bean: SourceBean) {
        KV.put(KVKeys.SHORT_DRAMA_API, bean.key.orEmpty())
    }

    fun clearSource() {
        KV.put(KVKeys.SHORT_DRAMA_API, "")
    }

    /**
     * 数据源 key（供列表加载使用）。
     */
    fun sourceKey(): String? = currentSource().key
}
