package com.example.pinga.model

data class WifiNetwork(
    val ssid: String?,
    val bssid: String,
    val rssiDbm: Int,
    val freqMhz: Int,
    val capabilities: String?
)