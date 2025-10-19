package com.example.pinga.ble

object OuiLookup {
    // Replace this with your real table; this stub just avoids NPEs.
    fun vendorFor(mac: String): String? {
        val pfx = mac.take(8).uppercase() // "AA:BB:CC"
        return localOuiTable[pfx]
    }

    // Minimal local table; populate as you like.
    private val localOuiTable: Map<String, String> = mapOf(
        // "D0:03:4B" to "Samsung Electronics Co.,Ltd",
        // "F4:0F:24" to "LG Electronics",
    )
}