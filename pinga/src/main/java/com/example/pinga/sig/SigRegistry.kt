package com.example.pinga.sig

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Offline SIG registry for Bluetooth Company IDs and Services.
 * - Loads once from assets/sig_companies.json and assets/sig_services.json
 * - Supports both map and array formats (e.g., Nordic's database)
 * - Safe: if files are missing/corrupt, returns empty maps (UI shows raw values)
 */
class SigRegistry private constructor(
    private val companies: Map<Int, String>,
    private val services: Map<String, String>
) {
    fun companyNameOrNull(companyId: Int): String? = companies[companyId]

    /** Accepts 16-bit ("FD5A") or full UUID ("0000fd5a-0000-1000-8000-00805f9b34fb"). */
    fun serviceNameOrNull(uuid: UUID): String? {
        uuidTo16(uuid)?.let { short ->
            services[short.uppercase()]?.let { return it }
        }
        return services[uuid.toString().uppercase()]
    }

    companion object {
        @Volatile private var INSTANCE: SigRegistry? = null

        fun get(context: Context): SigRegistry =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context.applicationContext).also { INSTANCE = it }
            }

        private fun build(ctx: Context): SigRegistry {
            val companies = loadJsonMap(ctx, "sig_companies.json")
                .mapNotNull { (k, v) -> parseCompanyId(k.trim())?.let { it to v } }
                .toMap()

            val services = loadJsonMap(ctx, "sig_services.json")
                .mapKeys { it.key.uppercase() }

            Log.d("SigRegistry", "Loaded companies=${companies.size}, services=${services.size}")
            Log.d("SIG/INIT", "apple.byDec=${companies[76]}")
            Log.d("SIG/INIT", "apple.byHex=${companies[0x004C]}")
            Log.d("SIG/INIT", "apple=${if (companies[76] != null) "FOUND" else "MISSING"}")
            Log.d("SIG/INIT", "companies=${companies.size} services=${services.size}")
            Log.d("SIG/INIT", "apple=${companies[76] ?: "MISSING"}")
            return SigRegistry(companies, services)
        }

        private fun parseCompanyId(raw: String): Int? = try {
            val s = raw.trim().removePrefix("0x").removePrefix("0X")
            // if the key has any letters A–F, assume hexadecimal
            val radix = if (s.any { it in 'A'..'F' || it in 'a'..'f' }) 16 else 10
            s.toInt(radix)
        } catch (_: Exception) { null }

        /**
         * Loads a JSON map from assets. Handles both:
         * - Map format: { "76": "Apple, Inc." }
         * - Array format: [ { "code":76, "name":"Apple, Inc." }, … ]
         */
        private fun loadJsonMap(ctx: Context, assetName: String): Map<String, String> {
            return try {
                val raw = ctx.assets.open(assetName).bufferedReader(Charsets.UTF_8).use { it.readText() }
                val text = sanitizeJson(raw)
                val trimmed = text.trimStart()

                val firstChar = trimmed.firstOrNull()
                if (firstChar == '{') {
                    val obj = JSONObject(trimmed)
                    buildMap {
                        val it = obj.keys()
                        while (it.hasNext()) {
                            val k = it.next()
                            put(k, obj.optString(k))
                        }
                    }
                } else if (firstChar == '[') {
                    val arr = JSONArray(trimmed)
                    buildMap {
                        for (i in 0 until arr.length()) {
                            val item = arr.optJSONObject(i)
                            if (item != null) {
                                val code = item.optInt("code", -1)
                                val name = item.optString("name", "")
                                if (code >= 0 && name.isNotBlank()) {
                                    put(code.toString(), name)
                                }
                            }
                        }
                    }
                } else {
                    Log.w("SigRegistry", "Unknown JSON format in $assetName")
                    emptyMap()
                }
            } catch (e: Exception) {
                Log.w("SigRegistry", "Failed to load $assetName (${e.message}). Using empty map.")
                emptyMap()
            }
        }

        private fun sanitizeJson(s: String): String {
            var t = s
            if (t.isNotEmpty() && t[0] == '\uFEFF') t = t.substring(1) // Strip BOM
            t = t.replace('“', '"').replace('”', '"').replace('’', '\'')
            t = t.replace(Regex("""(?m)^\s*//.*$"""), "") // line comments
            t = t.replace(Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL), "") // block comments
            t = t.replace(Regex(""",\s*([}\]])"""), "$1") // trailing commas
            t = t.replace(Regex("""\\(?=("?\s*[},]))"""), "") // stray backslashes
            return t.trim()
        }

        /** Returns 16-bit hex (e.g., "FD5A") if UUID is in Bluetooth Base space, else null. */
        private fun uuidTo16(u: UUID): String? {
            val base = UUID.fromString("00000000-0000-1000-8000-00805f9b34fb")
            if (u.leastSignificantBits != base.leastSignificantBits) return null
            val msbMask = 0x0000FFFF00000000L
            val msbBase = base.mostSignificantBits and -0x1000000000000L
            val msbU = u.mostSignificantBits and -0x1000000000000L
            if (msbU != msbBase) return null
            val shortVal = ((u.mostSignificantBits and msbMask) ushr 32).toInt()
            return String.format("%04X", shortVal)
        }
    }
}
