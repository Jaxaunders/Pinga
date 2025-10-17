package com.example.pinga.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.pinga.model.BleAdvert
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BleScanner(private val context: Context) {

    fun scanAsFlow(): Flow<BleAdvert> = callbackFlow {
        val hasScanPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasScanPerm) {
            Log.w("Pinga/BLE", "BLUETOOTH_SCAN permission missing")
            close(); return@callbackFlow
        }

        val mgr = context.getSystemService(BluetoothManager::class.java)
        val adapter: BluetoothAdapter? = mgr?.adapter
        val scanner = adapter?.bluetoothLeScanner
        if (adapter == null || !adapter.isEnabled || scanner == null) {
            Log.w("Pinga/BLE", "Adapter disabled or scanner null")
            close(); return@callbackFlow
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val cb = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, r: ScanResult) {
                val sr = r.scanRecord
                val manu = buildMap {
                    val msd = sr?.manufacturerSpecificData
                    if (msd != null && msd.size() > 0) {
                        for (i in 0 until msd.size()) {
                            val id = msd.keyAt(i)
                            val bytes = msd.valueAt(i)
                            if (bytes != null) put(id, bytes)
                        }
                    }
                }
                trySend(
                    BleAdvert(
                        address = r.device?.address ?: "",
                        timestampNanos = r.timestampNanos,
                        rssi = r.rssi,
                        isConnectable = r.isConnectable == true,
                        primaryPhy = r.primaryPhy,
                        secondaryPhy = r.secondaryPhy,
                        txPower = r.txPower,
                        serviceUuids = sr?.serviceUuids?.map { it.uuid.toString() } ?: emptyList(),
                        manufacturer = manu,
                        rawBytes = sr?.bytes,
                        advertisedName = sr?.deviceName ?: r.device?.name
                    )
                )
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("Pinga/BLE", "Scan failed: $errorCode")
            }
        }

        Log.d("Pinga/BLE", "startScan")
        try {
            scanner.startScan(/* filters */ null, settings, cb)
            awaitClose {
                Log.d("Pinga/BLE", "awaitClose: stopScan")
                runCatching { scanner.stopScan(cb) }
            }
        } finally {
            // Defensive: also stop here in case awaitClose wasn't invoked
            Log.d("Pinga/BLE", "finally: stopScan")
            runCatching { scanner.stopScan(cb) }
        }
    }
}
