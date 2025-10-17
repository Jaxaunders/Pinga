package com.example.pinga.model

data class BleAdvert(
    val address: String,
    val timestampNanos: Long,
    val rssi: Int,
    val isConnectable: Boolean,
    val primaryPhy: Int,
    val secondaryPhy: Int,
    val txPower: Int?,
    val serviceUuids: List<String>,
    val manufacturer: Map<Int, ByteArray>,
    val rawBytes: ByteArray?,
    val advertisedName: String?
)