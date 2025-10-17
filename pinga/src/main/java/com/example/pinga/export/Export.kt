package com.example.pinga.export

import com.example.pinga.model.*
import org.json.JSONArray
import org.json.JSONObject

object Export {
    fun toJson(s: Snapshot): JSONObject = JSONObject()
        .put("timestampIso", s.timestampIso)
        .put("startedAtNanos", s.startedAtNanos)
        .put("durationSec", s.durationSec)
        .put("gps", s.gps?.let { JSONObject().put("lat", it.lat).put("lon", it.lon).put("accM", it.accuracyM) } ?: JSONObject.NULL)
        .put("ble", JSONArray().apply {
            s.ble.forEach { adv ->
                put(JSONObject()
                    .put("address", adv.address)
                    .put("timestampNanos", adv.timestampNanos)
                    .put("rssi", adv.rssi)
                    .put("isConnectable", adv.isConnectable)
                    .put("primaryPhy", adv.primaryPhy)
                    .put("secondaryPhy", adv.secondaryPhy)
                    .put("txPower", adv.txPower ?: JSONObject.NULL)
                    .put("serviceUuids", JSONArray(adv.serviceUuids))
                    .put("advertisedName", adv.advertisedName ?: JSONObject.NULL))
            }
        })
        .put("wifi", JSONArray().apply {
            s.wifi.forEach { ap ->
                put(JSONObject()
                    .put("ssid", ap.ssid ?: "")
                    .put("bssid", ap.bssid)
                    .put("rssi", ap.rssiDbm)
                    .put("freqMhz", ap.freqMhz)
                    .put("capabilities", ap.capabilities ?: JSONObject.NULL))
            }
        })
}
