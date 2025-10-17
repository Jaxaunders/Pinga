package com.example.pinga.model
import com.example.pinga.core.Time

data class Snapshot(
    val timestampIso: String = Time.nowIso(),
    val startedAtNanos: Long,
    val durationSec: Int,
    val ble: List<BleAdvert>,
    val wifi: List<WifiNetwork> = emptyList(),
    val gps: GpsFix? = null
)