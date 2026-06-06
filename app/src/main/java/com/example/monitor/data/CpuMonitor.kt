package com.example.monitor.data

import java.io.BufferedReader
import java.io.FileReader

object CpuMonitor {

    data class CpuInfo(
        val totalPercent: Float,
        val perCore: List<Float>
    )

    private var prevTotal: Long = 0
    private var prevIdle: Long = 0
    private var prevCores: Map<Int, Pair<Long, Long>> = emptyMap()
    private var first = true

    fun read(): CpuInfo {
        try {
            val reader = BufferedReader(FileReader("/proc/stat"))
            val lines = reader.readLines()
            reader.close()

            var total: Long = 0
            var idle: Long = 0
            val cores = mutableMapOf<Int, Pair<Long, Long>>()

            for (line in lines) {
                if (line.startsWith("cpu")) {
                    val parts = line.split("\\s+".toRegex())
                    if (parts.size < 5) continue
                    val vals = parts.drop(1).mapNotNull { it.toLongOrNull() }
                    if (vals.isEmpty()) continue
                    val t = vals.sum()
                    val i = vals.getOrElse(3) { 0L }

                    if (parts[0] == "cpu") {
                        total = t
                        idle = i
                    } else {
                        val idx = parts[0].removePrefix("cpu").toIntOrNull() ?: continue
                        cores[idx] = Pair(t, i)
                    }
                }
            }

            if (first) {
                prevTotal = total
                prevIdle = idle
                prevCores = cores
                first = false
                return CpuInfo(0f, cores.keys.sorted().map { 0f })
            }

            val dTotal = total - prevTotal
            val dIdle = idle - prevIdle
            val totalPct = if (dTotal > 0) ((dTotal - dIdle) * 100f / dTotal) else 0f

            val corePct = cores.keys.sorted().map { idx ->
                val prev = prevCores[idx] ?: Pair(0L, 0L)
                val dt = cores[idx]!!.first - prev.first
                val di = cores[idx]!!.second - prev.second
                if (dt > 0) ((dt - di) * 100f / dt) else 0f
            }

            prevTotal = total
            prevIdle = idle
            prevCores = cores

            return CpuInfo(totalPct, corePct)
        } catch (_: Exception) {
            return CpuInfo(0f, emptyList())
        }
    }
}
