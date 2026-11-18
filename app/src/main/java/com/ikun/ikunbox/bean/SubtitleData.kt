package com.ikun.ikunbox.bean

class SubtitleData {

    @get:JvmName("getIsNew")
    @set:JvmName("setIsNew")
    var isNew: Boolean? = null

    var subtitleList: List<SourceSubtitle>? = null

    @get:JvmName("getIsZip")
    @set:JvmName("setIsZip")
    var isZip: Boolean? = null

    override fun toString(): String {
        return "SubtitleData{isNew='$isNew'}"
    }
}
