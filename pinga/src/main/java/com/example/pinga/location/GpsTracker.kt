package com.example.pinga.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.example.pinga.model.GpsFix

class GpsTracker(private val ctx: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(ctx)

    @SuppressLint("MissingPermission")
    fun fixes(): Flow<GpsFix> = callbackFlow {
        // Faster + more reliable for demos and indoor-ish scenarios
        val req = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, /* interval */ 3_000L
        )
            .setMinUpdateIntervalMillis(1_500L)
            .setWaitForAccurateLocation(true)
            .build()

        val cb = object : LocationCallback() {
            override fun onLocationResult(r: LocationResult) {
                r.lastLocation?.let { loc ->
                    trySend(GpsFix(loc.latitude, loc.longitude, loc.accuracy))
                }
            }
        }

        // 1) Seed quickly with a one-shot current location
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    trySend(GpsFix(loc.latitude, loc.longitude, loc.accuracy))
                } else {
                    // 2) Fallback to last known (often non-null even indoors)
                    client.lastLocation.addOnSuccessListener { last ->
                        if (last != null) {
                            trySend(GpsFix(last.latitude, last.longitude, last.accuracy))
                        }
                    }
                }
            }

        // 3) Begin continuous updates
        client.requestLocationUpdates(req, cb, Looper.getMainLooper())

        awaitClose { client.removeLocationUpdates(cb) }
    }
}
