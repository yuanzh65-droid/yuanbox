package com.yuanbox.yuanbox.data

import com.yuanbox.yuanbox.bean.VodInfo

interface PlaybackDataPorts {

    fun currentVod(): VodInfo?

    fun isLiveMode(): Boolean

    fun hasAttachedPage(): Boolean

    fun discardStartedContentOf(owners: List<String>)

    fun onProgressKeysCleared(keys: List<String>)
}

object PlaybackPorts {

    @Volatile
    var ports: PlaybackDataPorts? = null
}
