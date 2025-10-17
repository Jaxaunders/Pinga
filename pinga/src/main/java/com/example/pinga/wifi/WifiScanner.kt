package com.example.pinga.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.example.pinga.model.WifiNetwork

class WifiScanner(private val ctx: Context) {
    private var lastNonEmpty: List<WifiNetwork> = emptyList()

    fun scans(): Flow<List<WifiNetwork>> = callbackFlow {
        val appCtx = ctx.applicationContext
        val wm = appCtx.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val rx = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) {
                val mapped = wm.scanResults.map { it.toModel() }
                val stable = if (mapped.isNotEmpty()) mapped else lastNonEmpty
                if (stable.isNotEmpty()) lastNonEmpty = stable
                trySend(stable)
            }
        }

        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        appCtx.registerReceiver(rx, intentFilter)
        runCatching { wm.startScan() }

        try {
            awaitClose {
                runCatching { appCtx.unregisterReceiver(rx) }
            }
        } finally {
            // Defensive: ensure receiver is unregistered even if channel closed abnormally
            runCatching { appCtx.unregisterReceiver(rx) }
        }
    }

    private fun ScanResult.toModel() = WifiNetwork(
        ssid = SSID.takeIf { it.isNotBlank() },
        bssid = BSSID,
        rssiDbm = level,
        freqMhz = frequency,
        capabilities = capabilities
    )
}
