package com.example.pinga.ble

import com.example.pinga.model.BleAdvert
import kotlin.math.max

/**
 * Group BLE adverts into clusters and keep only a rolling time window.
 */
data class Cluster(
    val id: String,
    val members: MutableList<BleAdvert> = mutableListOf()
) {
    val size: Int get() = members.size
    val lastTsNanos: Long get() = if (members.isEmpty()) 0 else members.last().timestampNanos
}

/**
 * Rolling store keyed by a stable device/group identifier.
 * - key uses MAC if present, else falls back to first manufacturer id.
 * - items older than windowSec are pruned on every insert.
 */
class RollingWindow(private val windowSec: Int) {

    private val clustersById = LinkedHashMap<String, Cluster>()
    private val windowNanos = windowSec * 1_000_000_000L

    /** Derive a stable grouping key. */
    private fun keyOf(adv: BleAdvert): String {
        return if (adv.address.isNotBlank()) {
            "MAC:${adv.address}"
        } else {
            val manuId = adv.manufacturer.keys.firstOrNull() ?: -1
            "VENDOR:$manuId"
        }
    }

    /**
     * Add a single advert into the window. Also prunes old data.
     */
    fun add(adv: BleAdvert) {
        val k = keyOf(adv)
        val c = clustersById.getOrPut(k) { Cluster(k) }
        c.members += adv
        prune(adv.timestampNanos)
    }

    /**
     * Remove adverts older than the rolling horizon.
     * Drops empty clusters to avoid unbounded growth.
     */
    private fun prune(nowNanos: Long) {
        val cutoff = nowNanos - windowNanos
        val it = clustersById.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            val cl = entry.value
            // drop old members in-place
            if (cl.members.isNotEmpty()) {
                // members are appended in time order by add(); find first index newer than cutoff
                var firstKeep = 0
                val list = cl.members
                while (firstKeep < list.size && list[firstKeep].timestampNanos < cutoff) {
                    firstKeep++
                }
                if (firstKeep > 0) {
                    // remove [0, firstKeep)
                    list.subList(0, firstKeep).clear()
                }
            }
            if (cl.members.isEmpty()) it.remove()
        }
    }

    /** Snapshot of current clusters. */
    fun clusters(): List<Cluster> = clustersById.values.toList()

    /** Flattened list of all adverts (sorted by timestamp). */
    fun flat(): List<BleAdvert> =
        clustersById.values.asSequence().flatMap { it.members.asSequence() }
            .sortedBy { it.timestampNanos }.toList()

    /** Optional: current horizon in seconds. */
    fun windowSeconds(): Int = windowSec

    /** Count of clusters (for debugging/status). */
    fun clusterCount(): Int = clustersById.size
}
