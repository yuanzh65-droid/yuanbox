package com.ikun.ikunbox.data

import com.ikun.ikunbox.event.UiChange
import com.ikun.ikunbox.event.UiEvents

import com.ikun.ikunbox.bean.VodInfo
import com.ikun.ikunbox.util.AppLog
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.runBlocking

object HistoryWriter {

    private val writer: ExecutorService by lazy {
        Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "vod-history-writer") }
    }

    fun write(sourceKey: String, info: VodInfo) {
        writer.execute { writeNow(sourceKey, info) }
    }

    fun touch(sourceKey: String?, info: VodInfo?) {
        val vod = info ?: return
        val vodId = vod.id?.takeIf { it.isNotEmpty() }
        val key = resolveWriteKey(sourceKey, vod)
        if (vodId == null || key.isEmpty()) {
            AppLog.i("echo-history touch skipped key=" + key + " id=" + vod.id)
            return
        }
        writer.execute { touchNow(key, vodId) }
    }

    internal fun resolveWriteKey(sourceKey: String?, info: VodInfo): String =
        sourceKey?.takeIf { it.isNotEmpty() } ?: info.sourceKey.orEmpty()

    private fun touchNow(sourceKey: String, vodId: String) {
        val updated = try {
            runBlocking { AppGraph.historyRepository.touchVodRecord(sourceKey, vodId) }
        } catch (th: Throwable) {
            AppLog.e("HistoryWriter", "echo-history touch failed: " + th, th)
            return
        }
        if (!updated) {
            AppLog.i("echo-history touch no-row key=" + sourceKey + "|" + vodId)
            return
        }
        AppLog.i("echo-history touch key=" + sourceKey + "|" + vodId)
        UiEvents.post(UiChange.HistoryChanged)
    }

    private fun writeNow(sourceKey: String, info: VodInfo) {
        try {
            runBlocking { AppGraph.historyRepository.insertVodRecord(sourceKey, info) }
        } catch (th: Throwable) {
            AppLog.e("HistoryWriter", "echo-history insert failed: " + th, th)
            return
        }
        UiEvents.post(UiChange.HistoryChanged)
    }
}
