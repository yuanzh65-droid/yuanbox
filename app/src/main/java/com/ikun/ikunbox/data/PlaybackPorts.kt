package com.ikun.ikunbox.data

import com.ikun.ikunbox.bean.VodInfo

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
