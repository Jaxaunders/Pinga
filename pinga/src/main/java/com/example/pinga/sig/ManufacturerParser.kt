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

        var bestIdx = 0
        var bestScore = Int.MIN_VALUE

        for (i in 0 until mfg.size()) {
            val id = mfg.keyAt(i)
            val bytes = mfg.valueAt(i)
            val nameKnown = nameLookup(id) != null
            val payloadLen = bytes?.size ?: 0

            // Score: prefer known-name entries, then longer payloads
            val score = (if (nameKnown) 1_000_000 else 0) + payloadLen
            if (score > bestScore) {
                bestScore = score
                bestIdx = i
            }
        }

        val companyId = mfg.keyAt(bestIdx)
        val full = mfg.valueAt(bestIdx) ?: ByteArray(0)

        // Strip duplicated 2-byte ID if present (accept LE or BE)
        val payload = if (full.size >= 2) {
            val b0 = full[0].toInt() and 0xFF
            val b1 = full[1].toInt() and 0xFF
            val idLE = (b0) or (b1 shl 8)
            val idBE = (b1) or (b0 shl 8)
            if ((idLE == companyId || idBE == companyId) && full.size > 2) {
                full.copyOfRange(2, full.size)
            } else {
                full
            }
        } else {
            full
        }

        return ManufacturerInfo(
            companyId = companyId,
            companyName = nameLookup(companyId),
            payloadHex = payload.joinToString("") { "%02X".format(it) }
        )
    }
}