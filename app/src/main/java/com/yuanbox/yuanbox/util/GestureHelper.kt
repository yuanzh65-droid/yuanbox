package com.yuanbox.yuanbox.util

object GestureHelper {

    @JvmStatic
    fun isControlDisabled(): Boolean {
        return KV.get(KVKeys.GESTURE_CONTROL_DISABLED, false)
    }
}
