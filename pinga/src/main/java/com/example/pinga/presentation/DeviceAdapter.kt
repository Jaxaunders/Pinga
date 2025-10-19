package com.example.pinga.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pinga.R
import com.example.pinga.data.DeviceRow

class DeviceAdapter : ListAdapter<DeviceRow, DeviceAdapter.Holder>(DIFF) {
    object DIFF : DiffUtil.ItemCallback<DeviceRow>() {
        override fun areItemsTheSame(a: DeviceRow, b: DeviceRow) = a.mac == b.mac
        override fun areContentsTheSame(a: DeviceRow, b: DeviceRow) = a == b
    }

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        private val tName = v.findViewById<TextView>(R.id.tName)
        private val tBadges = v.findViewById<TextView>(R.id.tBadges)
        private val tMeta = v.findViewById<TextView>(R.id.tMeta)
        private val tServices = v.findViewById<TextView>(R.id.tServices)

        fun bind(row: DeviceRow) {
            // Device name
            tName.text = row.title

            // Manufacturer / vendor
            tBadges.text = row.vendorName ?: ""

            // MAC address + approximate distance
            val distanceText = if (row.estMeters >= 0) "Approx. %.1f m".format(row.estMeters) else "Distance unavailable"
            tMeta.text = "${row.mac}  •  $distanceText"

            // Services (only shown if available)
            if (row.serviceNames.isNotEmpty()) {
                tServices.visibility = View.VISIBLE
                tServices.text = "Services: ${row.serviceNames.joinToString(", ")}"
            } else {
                tServices.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_device, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) =
        holder.bind(getItem(position))
}
