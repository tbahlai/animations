package com.tsynytsyna.animations.core

import android.app.ActivityManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Categorizes the runtime Android hardware profile to dynamically configure
 * simulation Level of Detail (LOD), preventing thermal throttling, frame drops,
 * and memory pressure on low-tier devices.
 */
enum class PerformanceTier {
    LOW,
    MID,
    HIGH;

    val isBudget: Boolean get() = this == LOW
}

object DeviceTierManager {

    private var cachedTier: PerformanceTier? = null

    /**
     * Determines performance tier based on available CPU cores, max heap memory,
     * and low-RAM device flags.
     */
    fun getTier(context: Context? = null): PerformanceTier {
        cachedTier?.let { return it }

        val cores = Runtime.getRuntime().availableProcessors()
        val maxMemoryMb = Runtime.getRuntime().maxMemory() / (1024 * 1024)

        var isLowRam = false
        if (context != null) {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (am != null) {
                isLowRam = am.isLowRamDevice
            }
        }

        val tier = when {
            isLowRam || cores <= 4 || maxMemoryMb < 192 -> PerformanceTier.LOW
            cores <= 6 || maxMemoryMb < 384 -> PerformanceTier.MID
            else -> PerformanceTier.HIGH
        }

        cachedTier = tier
        return tier
    }

    /**
     * Recommended boid count for the particle swarm simulation.
     */
    fun getRecommendedBoidCount(tier: PerformanceTier): Int = when (tier) {
        PerformanceTier.LOW -> 600
        PerformanceTier.MID -> 1200
        PerformanceTier.HIGH -> 2000
    }

    /**
     * Recommended spring relaxation passes per frame for cloth physics.
     */
    fun getRecommendedClothIterations(tier: PerformanceTier): Int = when (tier) {
        PerformanceTier.LOW -> 2
        PerformanceTier.MID -> 3
        PerformanceTier.HIGH -> 5
    }

    /**
     * Recommended initial droplet count for fluid metaballs.
     */
    fun getRecommendedDropletCount(tier: PerformanceTier): Int = when (tier) {
        PerformanceTier.LOW -> 6
        PerformanceTier.MID -> 10
        PerformanceTier.HIGH -> 14
    }

    /**
     * Recommended gravitational grid resolution.
     */
    fun getRecommendedLensingGridSubdivisions(tier: PerformanceTier): Int = when (tier) {
        PerformanceTier.LOW -> 14
        PerformanceTier.MID -> 18
        PerformanceTier.HIGH -> 24
    }
}

/**
 * Remembers the device performance tier in Compose compositions.
 */
@Composable
fun rememberPerformanceTier(): PerformanceTier {
    val context = LocalContext.current.applicationContext
    return remember { DeviceTierManager.getTier(context) }
}
