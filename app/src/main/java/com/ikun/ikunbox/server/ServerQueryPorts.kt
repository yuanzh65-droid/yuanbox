package com.ikun.ikunbox.server

interface ServerQueryPort {

    fun dashData(): String?

    fun proxyLocal(params: MutableMap<String, String>): Array<Any?>?
}

object ServerQueryPorts {

    @Volatile
    var ports: ServerQueryPort? = null
}
