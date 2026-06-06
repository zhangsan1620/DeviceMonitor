package com.example.monitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monitor.data.CpuMonitor
import com.example.monitor.data.MemMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MonitorScreen()
            }
        }
    }
}

@Composable
fun MonitorScreen() {
    var cpuInfo by remember { mutableStateOf(CpuMonitor.CpuInfo(0f, emptyList())) }
    var memInfo by remember { mutableStateOf(MemMonitor.MemInfo(0, 0, 0, 0f, 0)) }
    var history by remember { mutableStateOf(listOf<Float>()) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        while (isActive) {
            cpuInfo = CpuMonitor.read()
            memInfo = MemMonitor.read(context)
            history = (history + cpuInfo.totalPercent).takeLast(60)
            delay(1500)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("System Monitor", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        CpuSection(cpuInfo, history)
        Spacer(Modifier.height(16.dp))
        MemorySection(memInfo)
        Spacer(Modifier.height(16.dp))
        LegendSection()
    }
}

@Composable
fun CpuSection(info: CpuMonitor.CpuInfo, history: List<Float>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("CPU", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Total: ${"%.1f".format(info.totalPercent)}%",
                fontFamily = FontFamily.Monospace,
                fontSize = 24.sp
            )
            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = info.totalPercent / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (info.totalPercent < 50) Color(0xFF4CAF50)
                        else if (info.totalPercent < 80) Color(0xFFFFA726)
                        else Color(0xFFE53935),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(Modifier.height(12.dp))
            Text("Per Core:", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))

            for ((i, pct) in info.perCore.withIndex()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CPU$i", fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                        modifier = Modifier.width(40.dp))
                    LinearProgressIndicator(
                        progress = pct / 100f,
                        modifier = Modifier.weight(1f).height(6.dp),
                        color = Color(0xFF42A5F5),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Text("${"%.0f".format(pct)}%", fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp, modifier = Modifier.width(42.dp).padding(start = 4.dp))
                }
            }

            if (history.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("History (60s):", style = MaterialTheme.typography.bodyMedium)
                CpuHistoryChart(history)
            }
        }
    }
}

@Composable
fun CpuHistoryChart(data: List<Float>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(64.dp)) {
        if (data.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height
        val step = w / (data.size - 1).coerceAtLeast(1)
        for (i in 0 until data.size - 1) {
            val x1 = i * step
            val y1 = h - (data[i] / 100f * h).coerceIn(0f, h)
            val x2 = (i + 1) * step
            val y2 = h - (data[i + 1] / 100f * h).coerceIn(0f, h)
            drawLine(
                color = Color(0xFF42A5F5),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun MemorySection(info: MemMonitor.MemInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Memory", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Text(
                text = "${"%.0f".format(info.percent)}% used",
                fontFamily = FontFamily.Monospace,
                fontSize = 24.sp
            )
            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = info.percent / 100f,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (info.percent < 50) Color(0xFF4CAF50)
                        else if (info.percent < 80) Color(0xFFFFA726)
                        else Color(0xFFE53935),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(Modifier.height(12.dp))
            val items = listOf(
                "Used" to "${info.usedMb} MB",
                "Available" to "${info.availMb} MB",
                "Total" to "${info.totalMb} MB"
            )
            for ((label, value) in items) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Text(label, fontFamily = FontFamily.Monospace, modifier = Modifier.width(88.dp))
                    Text(value, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("App Native Heap: ${info.nativeHeapMb} MB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun LegendSection() {
    Text(
        text = "Updated every 1.5s | No root required",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
    )
}
