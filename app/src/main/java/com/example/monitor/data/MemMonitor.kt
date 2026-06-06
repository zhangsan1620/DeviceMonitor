package com.example.monitor.data

import android.app.ActivityManager
import android.content.Context
import android.os.Debug

object MemMonitor {

    data class MemInfo(
        val totalMb: Long,
        val usedMb: Long,
        val availMb: Long,
        val percent: Float,
        val nativeHeapMb: Long
    )

    fun read(context: Context): MemInfo {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)

            val totalMb = mi.totalMem / (1024 * 1024)
            val availMb = mi.availMem / (1024 * 1024)
            val usedMb = totalMb - availMb
            val percent = if (totalMb > 0) usedMb * 100f / totalMb else 0f
            val nativeHeapMb = Debug.getNativeHeapAllocatedSize() / (1024 * 1024)

            MemInfo(totalMb, usedMb, availMb, percent, nativeHeapMb)
        } catch (_: Exception) {
            MemInfo(0, 0, 0, 0f, 0)
        }
    }
}
