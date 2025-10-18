package com.example.pinga.sig

import android.util.SparseArray

data class ManufacturerInfo(
    val companyId: Int,
    val companyName: String?,
    val payloadHex: String // vendor payload (without the optional duplicated 2-byte ID)
)

object ManufacturerParser {
    /**
     * Parse the first Manufacturer Specific Data entry from ScanRecord.manufacturerSpecificData.
     * Android's SparseArray key is already the 16-bit Company Identifier.
     * Some vendors also include the 2 ID bytes at the start of the value; we strip them if present.
     */
    fun parseFirst(mfg: SparseArray<ByteArray>?, nameLookup: (Int) -> String?): ManufacturerInfo? {
        if (mfg == null || mfg.size() == 0) return null
        val companyId = mfg.keyAt(0)
        val full = mfg.valueAt(0) ?: return ManufacturerInfo(companyId, nameLookup(companyId), "")
        val payload = if (full.size >= 2) {
            val idLE = (full[0].toInt() and 0xFF) or ((full[1].toInt() and 0xFF) shl 8)
            if (idLE == companyId && full.size > 2) full.copyOfRange(2, full.size) else full
        } else full
        return ManufacturerInfo(
            companyId = companyId,
            companyName = nameLookup(companyId),
            payloadHex = payload.joinToString("") { "%02X".format(it) }
        )
    }
}