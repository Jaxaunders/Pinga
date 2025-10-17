package com.example.pinga.core
import android.Manifest

object Permissions {
    fun all(): Array<String> = buildList {
        add(Manifest.permission.BLUETOOTH_SCAN)
        add(Manifest.permission.BLUETOOTH_CONNECT)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_WIFI_STATE)
        add(Manifest.permission.CHANGE_WIFI_STATE)
        if (android.os.Build.VERSION.SDK_INT >= 33)
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
    }.toTypedArray()
}