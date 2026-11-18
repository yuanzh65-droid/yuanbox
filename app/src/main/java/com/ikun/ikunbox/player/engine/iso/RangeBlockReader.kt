package com.ikun.ikunbox.player.engine.iso

fun interface RangeBlockReader {
    fun read(start: Long, length: Int): ByteArray
}
