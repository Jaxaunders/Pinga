package com.example.pinga.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.example.pinga.model.GpsFix

class GpsTracker(private val ctx: Context) {
    @SuppressLint("MissingPermission")
    fun fixes(): Flow<GpsFix> = callbackFlow {
        val client = LocationServices.getFusedLocationProviderClient(ctx)
        val req = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000L
        ).build()
        val cb = object : LocationCallback() {
            override fun onLocationResult(r: LocationResult) {
                r.lastLocation?.let { loc ->
                    trySend(GpsFix(loc.latitude, loc.longitude, loc.accuracy))
                }
            }
        }

        client.requestLocationUpdates(req, cb, ctx.mainLooper)

        try {
            awaitClose { runCatching { client.removeLocationUpdates(cb) } }
        } finally {
            // Defensive: remove updates if awaitClose wasn’t reached
            runCatching { client.removeLocationUpdates(cb) }
        }
    }
}
