package com.yuanbox.yuanbox.ui.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuanbox.yuanbox.bean.Movie
import com.yuanbox.yuanbox.bean.MovieSort
import com.yuanbox.yuanbox.sourcedata.SourceViewModel
import com.yuanbox.yuanbox.util.AppLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * 短剧「推荐」流的洗牌引擎。
 *
 * 需求：
 * - 分类栏含真实分类 + 默认「推荐」；
 * - 推荐进入整批洗牌，按随机类目 / 页码取片；
 * - 切类也随机换页并后台掺新片，保证每次不重样；
 * - 队尾自动续批；真实分类翻下一页。
 *
 * 实现：推荐流从「真实短剧分类集合」中随机挑选类目，再随机选页码拉取，
 * 拉到的视频整批洗牌并过滤已展示过的（防重样），追加到推荐队列末尾。
 */
class ShortDramaRecommendViewModel : ViewModel() {

    sealed interface RecommendState {
        data object Loading : RecommendState
        data class Ready(val videos: List<Movie.Video>) : RecommendState
        data object Empty : RecommendState
        data class Error(val message: String) : RecommendState
    }

    private val svm = SourceViewModel()

    private val _recommendState = MutableStateFlow<RecommendState>(RecommendState.Loading)
    val recommendState: StateFlow<RecommendState> = _recommendState.asStateFlow()

    /** 推荐队列（已洗牌去重后的完整列表）。 */
    private val _queue = MutableStateFlow<List<Movie.Video>>(emptyList())
    val queue: StateFlow<List<Movie.Video>> = _queue.asStateFlow()

    /** 已展示过的视频 id，用于防重样。 */
    private val seen = HashSet<String>()

    /** 可用于推荐的短剧分类（真实分类）。 */
    private var candidateSorts: List<MovieSort.SortData> = emptyList()

    private var sourceKey: String? = null
    private var loadingBatch = false
    private var batchGeneration = 0

    init {
        viewModelScope.launch {
            svm.listResult.flow.collect { abs ->
                onListResult(abs)
            }
        }
    }

    fun configure(sourceKey: String?, sorts: List<MovieSort.SortData>) {
        this.sourceKey = sourceKey
        this.candidateSorts = sorts
    }

    /**
     * 启动推荐流：清空队列并从随机类目开始拉第一批。
     */
    fun start() {
        batchGeneration++
        seen.clear()
        _queue.value = emptyList()
        _recommendState.value = RecommendState.Loading
        fetchNextBatch()
    }

    /**
     * 切换分类时重新洗牌：随机换页 + 后台掺新片。
     */
    fun reshuffle() {
        batchGeneration++
        _queue.value = emptyList()
        _recommendState.value = RecommendState.Loading
        fetchNextBatch()
    }

    /**
     * 队尾自动续批：队列快耗尽时调用。
     */
    fun ensureMore() {
        if (_queue.value.size > 4) return
        fetchNextBatch()
    }

    private fun fetchNextBatch() {
        if (loadingBatch) return
        if (candidateSorts.isEmpty()) {
            _recommendState.value = RecommendState.Empty
            return
        }
        val key = sourceKey ?: return
        loadingBatch = true
        val generation = batchGeneration
        // 随机选类目 + 随机页码
        val sort = candidateSorts[Random.nextInt(candidateSorts.size)]
        val page = Random.nextInt(1, 6) // 随机 1~5 页，扩大覆盖面
        AppLog.i("echo-shortdrama-recommend: sort=${sort.id}/${sort.name} page=$page")
        svm.getList(key, sort, page)
        // 兜底：如果该请求未在超时内返回，重置 loading 标记
        viewModelScope.launch {
            kotlinx.coroutines.delay(15_000)
            if (generation == batchGeneration) {
                loadingBatch = false
                if (_recommendState.value is RecommendState.Loading) {
                    _recommendState.value = RecommendState.Empty
                }
            }
        }
    }

    private fun onListResult(abs: com.yuanbox.yuanbox.bean.AbsXml?) {
        loadingBatch = false
        val videos = abs?.movie?.videoList ?: emptyList()
        AppLog.i("echo-shortdrama-recommend-result: n=${videos.size}")
        if (videos.isEmpty()) {
            if (_queue.value.isEmpty()) {
                _recommendState.value = RecommendState.Empty
            }
            return
        }
        // 过滤已看 + 洗牌
        val fresh = videos.filter { v ->
            val id = v.id
            id != null && seen.add(id)
        }
        val shuffled = fresh.shuffled(Random)
        if (shuffled.isEmpty()) {
            // 全部重复，再尝试拉一批
            if (_queue.value.isEmpty()) fetchNextBatch()
            return
        }
        val merged = _queue.value + shuffled
        _queue.value = merged
        _recommendState.value = RecommendState.Ready(merged)
    }

    fun markSeen(video: Movie.Video) {
        video.id?.let { seen.add(it) }
    }

    fun clearSeen() {
        seen.clear()
    }
}
