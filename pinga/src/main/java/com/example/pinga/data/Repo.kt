package com.example.pinga.data

import android.content.Context
import com.example.pinga.ble.BleScanner
import com.example.pinga.ble.DistanceEstimator
import com.example.pinga.ble.RollingWindow
import com.example.pinga.model.*
import com.example.pinga.wifi.WifiScanner
import com.example.pinga.location.GpsTracker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class DeviceRow(
    val title: String,
    val mac: String,
    val rssi: Int,
    val estMeters: Double,
    val lastSeenSec: Int,
    val vendorName: String? = null,
    val serviceNames: List<String> = emptyList(),
    val mfgPayloadHex: String? = null
)

class Repo(ctx: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val ble = BleScanner(ctx)
    private val wifi = WifiScanner(ctx)
    private val gps  = GpsTracker(ctx)

    private val latestWifi = MutableStateFlow<List<WifiNetwork>>(emptyList())
    private val latestGps  = MutableStateFlow<GpsFix?>(null)

    private val window = RollingWindow(windowSec = 120)

    private var bleJob: Job? = null
    private var wifiJob: Job? = null
    private var gpsJob: Job? = null

    private val _rows = MutableStateFlow<List<DeviceRow>>(emptyList())
    val rows: StateFlow<List<DeviceRow>> = _rows.asStateFlow()

    private var captureStartedNanos: Long = 0L
    private var lastBleFlat: List<BleAdvert> = emptyList()

    fun start() {
        if (bleJob != null) return
        captureStartedNanos = System.nanoTime()

        wifiJob = wifi.scans()
            .onEach { if (it.isNotEmpty()) latestWifi.value = it }
            .launchIn(scope)

        gpsJob = gps.fixes()
            .onEach { latestGps.value = it }
            .launchIn(scope)

        bleJob = ble.scanAsFlow()
            .onEach { adv ->
                window.add(adv)
                val now = System.nanoTime()
                val rows = window.clusters().map { cl ->
                    val last = cl.members.maxBy { it.timestampNanos }
                    val d = DistanceEstimator.metersFromRssi(last.rssi, last.txPower)
                    val age = ((now - last.timestampNanos) / 1_000_000_000L).toInt().coerceAtLeast(0)
                    DeviceRow(
                        title = last.advertisedName ?: "Unknown",
                        mac = last.address,
                        rssi = last.rssi,
                        estMeters = d,
                        lastSeenSec = age
                    )
                }.sortedBy { it.estMeters }
                _rows.value = rows
                lastBleFlat = window.flat().sortedBy { it.timestampNanos }
            }
            .launchIn(scope)
    }

    fun stop() {
        bleJob?.cancel(); bleJob = null
        wifiJob?.cancel(); wifiJob = null
        gpsJob?.cancel(); gpsJob = null
    }

    fun buildSnapshot(): Snapshot {
        val elapsedSec = ((System.nanoTime() - captureStartedNanos) / 1_000_000_000L).toInt().coerceAtLeast(0)
        return Snapshot(
            startedAtNanos = captureStartedNanos,
            durationSec = elapsedSec,
            ble = lastBleFlat,
            wifi = latestWifi.value,
            gps  = latestGps.value
        )
    }
}
