package com.ikun.ikunbox.bean

import java.util.HashMap

class CastVideo(
    val url: String,
    val name: String?,
    rawHeaders: HashMap<String, String>?,
    val position: Long,
) {
    val headers: HashMap<String, String> = rawHeaders ?: HashMap()
}
