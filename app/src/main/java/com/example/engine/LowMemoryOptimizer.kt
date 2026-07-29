package com.example.engine

import android.content.Context
import android.os.Debug

enum class MemoryOptimizationLevel(val label: String, val description: String) {
    AGGRESSIVE("Aggressive (Older Hardware / < 2GB RAM)", "Automatically suspends inactive tabs after 3 minutes. Disables unnecessary visual animations."),
    BALANCED("Balanced (Standard)", "Suspends background tabs when memory usage exceeds 75%."),
    OFF("Performance Mode", "Keans all background tabs active in memory.")
}

data class MemoryStatus(
    val usedMemoryMb: Long,
    val maxMemoryMb: Long,
    val memoryUsagePercent: Int,
    val suspendedTabsCount: Int,
    val optimizationLevel: MemoryOptimizationLevel
)

object LowMemoryOptimizer {
    fun getSystemMemoryStatus(context: Context, suspendedCount: Int = 0, level: MemoryOptimizationLevel = MemoryOptimizationLevel.BALANCED): MemoryStatus {
        val runtime = Runtime.getRuntime()
        val allocated = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val maxMemory = runtime.maxMemory() / (1024 * 1024)
        val percent = if (maxMemory > 0) ((allocated.toDouble() / maxMemory.toDouble()) * 100).toInt() else 45

        return MemoryStatus(
            usedMemoryMb = allocated,
            maxMemoryMb = maxMemory,
            memoryUsagePercent = percent.coerceIn(10, 95),
            suspendedTabsCount = suspendedCount,
            optimizationLevel = level
        )
    }

    fun triggerGarbageCollection() {
        System.gc()
    }
}
