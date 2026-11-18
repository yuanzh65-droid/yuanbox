package com.ikun.ikunbox.ui

internal object DetailResponseGuard {

    fun isCurrent(requestToken: Int, responseToken: Int?): Boolean =
        responseToken != null && responseToken == requestToken

    fun isUnloadableTarget(vodId: String, sourceMissing: Boolean): Boolean =
        vodId.isEmpty() || vodId.startsWith("msearch:") || sourceMissing
}
