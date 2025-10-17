package com.example.pinga.ble
import kotlin.math.pow

object DistanceEstimator {
    // log-distance model; tune n if needed
    fun metersFromRssi(rssi: Int, txPowerDbm: Int? = null, n: Double = 2.0): Double {
        val r0 = txPowerDbm ?: -59
        val x = (r0 - rssi) / (10.0 * n)
        return 10.0.pow(x).coerceAtLeast(0.1)
    }
}