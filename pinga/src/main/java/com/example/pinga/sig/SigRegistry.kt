package com.example.pinga.sig

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.util.UUID

/**
 * Offline SIG registry for Bluetooth Company IDs and Services.
 * - Loads once from assets/sig_companies.json and assets/sig_services.json
 * - Safe: if files are missing/corrupt, returns empty maps (UI shows raw values)
 */
class SigRegistry private constructor(
    private val companies: Map<Int, String>,
    private val services: Map<String, String>
) {
    fun companyNameOrNull(companyId: Int): String? = companies[companyId]

    /** Accepts 16-bit ("FD5A") or full UUID ("0000fd5a-0000-1000-8000-00805f9b34fb"). */
    fun serviceNameOrNull(uuid: UUID): String? {
        // Try 16-bit short form first if in base UUID space
        uuidTo16(uuid)?.let { short ->
            services[short]?.let { return it }
        }
        // Then full UUID string
        return services[uuid.toString().lowercase()]
            ?: services[uuid.toString().uppercase()]
    }

    companion object {
        @Volatile private var INSTANCE: SigRegistry? = null

        fun get(context: Context): SigRegistry =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context.applicationContext).also { INSTANCE = it }
            }

        private fun build(ctx: Context): SigRegistry {
            val companies = loadJsonMap(ctx, "sig_companies.json")
                .mapKeys { it.key.toIntOrNull() ?: -1 }
                .filterKeys { it >= 0 }
            val services = loadJsonMap(ctx, "sig_services.json")
                .mapKeys { it.key.uppercase() }
            Log.d("SigRegistry", "Loaded companies=${companies.size}, services=${services.size}")
            return SigRegistry(companies, services)
        }

        private fun loadJsonMap(ctx: Context, assetName: String): Map<String, String> = try {
            val text = ctx.assets.open(assetName).bufferedReader().use { it.readText() }
            val obj = JSONObject(text)
            buildMap {
                val it = obj.keys()
                while (it.hasNext()) {
                    val k = it.next()
                    put(k, obj.optString(k))
                }
            }
        } catch (e: Exception) {
            Log.w("SigRegistry", "Failed to load $assetName (${e.message}). Using empty map.")
            emptyMap()
        }

        /** Returns 16-bit hex (e.g., "FD5A") if UUID is in Bluetooth Base space, else null. */
        private fun uuidTo16(u: UUID): String? {
            // Bluetooth Base UUID = 0000xxxx-0000-1000-8000-00805F9B34FB
            val base = UUID.fromString("00000000-0000-1000-8000-00805f9b34fb")
            val msbMask = 0x0000FFFF00000000L
            val msbBase = base.mostSignificantBits and -0x1000000000000L
            val msbU    = u.mostSignificantBits and -0x1000000000000L
            if (u.leastSignificantBits != base.leastSignificantBits || msbU != msbBase) return null
            val shortVal = ((u.mostSignificantBits and msbMask) ushr 32).toInt()
            return String.format("%04X", shortVal)
        }
    }
}