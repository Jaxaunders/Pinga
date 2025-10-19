package com.example.pinga.ble

import kotlin.math.pow

object DistanceEstimator {
    // Log-distance model with strict input guards
    fun metersFromRssi(rssi: Int, txPowerDbm: Int? = null, n: Double = 2.0): Double {
        // Ignore impossible/placeholder RSSI readings
        if (rssi !in -100..-30) return -1.0

        // Use txPower only if it's realistic; otherwise fall back to a sane reference
        val r0 = txPowerDbm?.takeIf { it in -100..-30 } ?: -59

        val ratio = (r0 - rssi) / (10.0 * n)
        val distance = 10.0.pow(ratio)

        // Clamp to a practical range; return -1 when it still looks nonsense
        val clamped = distance.coerceIn(0.1, 30.0)
        return if (clamped.isFinite()) clamped else -1.0
    }
}
