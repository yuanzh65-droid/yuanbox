package com.yuanbox.yuanbox.util

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Debug

/**
 * 追踪应用前后台状态与当前页面，为异常报告提供上下文（内存/前后台/当前页面）。
 */
object AppForegroundTracker {

    private var foregroundActivityCount = 0
    private var currentPageName: String = ""

    @Volatile
    private var appInForeground = false

    fun install(app: Application) {
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                foregroundActivityCount++
                appInForeground = true
                currentPageName = activity.javaClass.simpleName
            }

            override fun onActivityResumed(activity: Activity) {
                currentPageName = activity.javaClass.simpleName
            }

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                foregroundActivityCount--
                if (foregroundActivityCount <= 0) {
                    foregroundActivityCount = 0
                    appInForeground = false
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    fun isForeground(): Boolean = appInForeground

    fun currentPage(): String = currentPageName

    /**
     * 采集当前上下文快照：内存、前后台、当前页面。
     */
    fun snapshot(context: Context?): String {
        val sb = StringBuilder()
        sb.append("foreground=").append(appInForeground)
        sb.append("; page=").append(currentPageName.ifEmpty { "none" })
        try {
            if (context != null) {
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                if (am != null) {
                    val mi = ActivityManager.MemoryInfo()
                    am.getMemoryInfo(mi)
                    sb.append("; memAvail=").append(mi.availMem / 1024 / 1024).append("MB")
                    sb.append("; memLow=").append(mi.lowMemory)
                    sb.append("; memThreshold=").append(mi.threshold / 1024 / 1024).append("MB")
                }
            }
            val rt = Runtime.getRuntime()
            val used = rt.totalMemory() - rt.freeMemory()
            sb.append("; heapUsed=").append(used / 1024 / 1024).append("MB")
            sb.append("; heapMax=").append(rt.maxMemory() / 1024 / 1024).append("MB")
            sb.append("; nativeHeap=").append(Debug.getNativeHeapAllocatedSize() / 1024 / 1024).append("MB")
        } catch (ignored: Throwable) {
        }
        return sb.toString()
    }
}
