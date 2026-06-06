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
        val nativeHeapMb: Long,
        val dalvikHeapMb: Long,
        val otherMb: Long
    )

    fun read(context: Context): MemInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)

        val totalMb = mi.totalMem / (1024 * 1024)
        val availMb = mi.availMem / (1024 * 1024)
        val usedMb = totalMb - availMb
        val percent = if (totalMb > 0) usedMb * 100f / totalMb else 0f

        val dm = Debug.MemoryInfo()
        Debug.getMemoryInfo(dm)
        val nativeHeapMb = dm.nativeHeapAllocatedSize / (1024 * 1024)
        val dalvikHeapMb = dm.dalvikHeapAllocatedSize / (1024 * 1024)
        val otherMb = dm.getTotalAllocated() / (1024 * 1024) - nativeHeapMb - dalvikHeapMb

        return MemInfo(totalMb, usedMb, availMb, percent, nativeHeapMb, dalvikHeapMb, otherMb)
    }
}
