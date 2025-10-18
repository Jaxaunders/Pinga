package com.example.pinga.model

data class BleAdvert(
    val address: String,
    val timestampNanos: Long,
    val rssi: Int,
    val isConnectable: Boolean,
    val primaryPhy: Int,
    val secondaryPhy: Int,
    val txPower: Int,
    val serviceUuids: List<String>,
    val manufacturer: Map<Int, ByteArray>,
    val rawBytes: ByteArray?,
    val advertisedName: String?,
    /* ===== Resolved fields after SIG lookup ===== */
    /** e.g., 76 for Apple, 117 for Samsung (null if none present) */
    val resolvedManufacturerId: Int? = null,
    /** e.g., "Apple, Inc." (null if not resolved) */
    val resolvedManufacturerName: String? = null,
    /** Manufacturer payload after stripping duplicated 2-byte ID, as HEX (null if none) */
    val resolvedManufacturerPayloadHex: String? = null,
    /** Known service names derived from service UUIDs (e.g., ["Tile","Google Fast Pair"]) */
    val resolvedServiceNames: List<String> = emptyList()
)