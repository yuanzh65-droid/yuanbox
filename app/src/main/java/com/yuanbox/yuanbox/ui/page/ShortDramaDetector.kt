package com.yuanbox.yuanbox.ui.page

import com.yuanbox.yuanbox.bean.MovieSort

/**
 * 短剧内容识别：通过分类名称匹配短剧关键词。
 * 参考视频的短剧模式包含「推荐、男频、女频、都市」等分类，但跨源的短剧分类
 * 命名不一，因此用关键词匹配来识别当前视频源中的短剧分类。
 */
object ShortDramaDetector {

    private val KEYWORDS = listOf(
        "短剧",
        "短劇",
        "短剧热播",
        "短剧精选",
        "快手短剧",
        "抖音短剧",
        "微短剧",
        "爽剧",
        "短剧合集",
        "竖屏剧",
        "小剧场",
        "short drama",
        "minidrama",
        "duanju",
    )

    fun isShortDramaSort(sort: MovieSort.SortData?): Boolean {
        val name = sort?.name?.trim().orEmpty()
        if (name.isEmpty()) return false
        return KEYWORDS.any { name.contains(it, ignoreCase = true) }
    }

    fun filterShortDramaSorts(sorts: List<MovieSort.SortData>): List<MovieSort.SortData> =
        sorts.filter { isShortDramaSort(it) }
}
