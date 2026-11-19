package com.yuanbox.yuanbox.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuanbox.yuanbox.bean.AbsXml
import com.yuanbox.yuanbox.bean.VodInfo
import com.yuanbox.yuanbox.data.AppGraph
import com.yuanbox.yuanbox.player.PlaybackSession
import com.yuanbox.yuanbox.sourcedata.SourceViewModel
import com.yuanbox.yuanbox.ui.player.PlayContainer
import com.yuanbox.yuanbox.util.AppLog
import com.yuanbox.yuanbox.util.HistoryHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 短剧播放器 ViewModel：管理短剧列表、详情加载与播放会话构造。
 */
class ShortDramaPlayerViewModel : ViewModel() {

    sealed interface PlayerState {
        data object Loading : PlayerState
        data class Ready(val session: PlaybackSession, val title: String) : PlayerState
        data class Error(val message: String) : PlayerState
    }

    data class Item(val id: String, val sourceKey: String, val name: String, val pic: String)

    private val svm = SourceViewModel()

    private val _state = MutableStateFlow<PlayerState>(PlayerState.Loading)
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    var currentIndex: Int = 0
        private set

    private var items: List<Item> = emptyList()
    private var inited = false
    private var boundContainer: PlayContainer? = null

    init {
        viewModelScope.launch {
            svm.detailResult.flow.collect { abs ->
                onDetailResult(abs)
            }
        }
    }

    fun initFromIntent(intent: Intent?) {
        if (inited) return
        inited = true
        val ids = intent?.getStringArrayListExtra("sd_ids").orEmpty()
        val names = intent?.getStringArrayListExtra("sd_names").orEmpty()
        val pics = intent?.getStringArrayListExtra("sd_pics").orEmpty()
        val source = intent?.getStringExtra("sd_source").orEmpty()
        val index = intent?.getIntExtra("short_drama_index", 0) ?: 0
        items = ids.mapIndexed { i, id ->
            Item(
                id = id,
                sourceKey = source,
                name = names.getOrNull(i).orEmpty(),
                pic = pics.getOrNull(i).orEmpty(),
            )
        }
        currentIndex = index.coerceIn(0, (items.size - 1).coerceAtLeast(0))
        loadCurrent()
    }

    fun currentTitle(): String = items.getOrNull(currentIndex)?.name.orEmpty()

    fun next() {
        if (currentIndex >= items.size - 1) return
        currentIndex++
        loadCurrent()
    }

    fun previous() {
        if (currentIndex <= 0) return
        currentIndex--
        loadCurrent()
    }

    private fun loadCurrent() {
        val item = items.getOrNull(currentIndex) ?: run {
            _state.value = PlayerState.Error("empty")
            return
        }
        _state.value = PlayerState.Loading
        AppLog.i("echo-shortdrama-load: id=${item.id} idx=$currentIndex")
        svm.getDetail(item.sourceKey, item.id)
    }

    private fun onDetailResult(abs: AbsXml?) {
        val item = items.getOrNull(currentIndex) ?: return
        val detailVideo = abs?.movie?.videoList?.firstOrNull() ?: run {
            _state.value = PlayerState.Error("load failed")
            return
        }
        detailVideo.id = item.id
        if (detailVideo.name.isNullOrEmpty()) detailVideo.name = item.name
        if (detailVideo.pic.isNullOrEmpty()) detailVideo.pic = item.pic

        var info = VodInfo.fromVideo(detailVideo)
        info = info.copy(sourceKey = item.sourceKey)
        if (info.playFlag == null || info.seriesMap?.containsKey(info.playFlag) != true) {
            info = info.copy(playFlag = info.seriesMap?.keys?.firstOrNull())
        }
        if (info.seriesMap?.get(info.playFlag).isNullOrEmpty()) {
            _state.value = PlayerState.Error("no playable line")
            return
        }

        // 读取续播进度
        viewModelScope.launch {
            val record = withContext(Dispatchers.IO) {
                if (HistoryHelper.isIncognito()) null
                else AppGraph.historyRepository.getVodInfo(item.sourceKey, item.id)
            }
            var final = info
            if (record != null) {
                final = final.copy(
                    playIndex = (record.playIndex).coerceIn(0, (final.seriesMap?.get(final.playFlag)?.size ?: 1) - 1),
                    playFlag = record.playFlag ?: final.playFlag,
                    playerCfg = record.playerCfg,
                    reverseSort = record.reverseSort,
                )
            }
            if (final.reverseSort) final.reverse()
            if (final.playFlag == null || final.seriesMap?.containsKey(final.playFlag) != true) {
                final = final.copy(playFlag = final.seriesMap?.keys?.firstOrNull())
            }
            val list = final.seriesMap?.get(final.playFlag)
            if (list.isNullOrEmpty()) {
                _state.value = PlayerState.Error("no playable line")
                return@launch
            }
            final = final.copy(playIndex = final.playIndex.coerceIn(0, list.size - 1))
            val session = PlaybackSession(final, item.sourceKey, false)
            _state.value = PlayerState.Ready(session, item.name)
        }
    }

    fun bindContainer(container: PlayContainer, session: PlaybackSession) {
        if (boundContainer === container) return
        boundContainer = container
        container.setData(session)
    }

    override fun onCleared() {
        super.onCleared()
        boundContainer = null
    }
}
