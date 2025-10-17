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
        private val tMac  = v.findViewById<TextView>(R.id.tMac)
        private val tRssi = v.findViewById<TextView>(R.id.tRssi)
        private val tDist = v.findViewById<TextView>(R.id.tDist)
        private val tSeen = v.findViewById<TextView>(R.id.tSeen)
        fun bind(row: DeviceRow) {
            tName.text = row.title
            tMac.text  = row.mac
            tRssi.text = row.rssi.toString()
            tDist.text = String.format("%.1f", row.estMeters)
            tSeen.text = row.lastSeenSec.toString()
        }
    }
    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        Holder(LayoutInflater.from(p.context).inflate(R.layout.row_device, p, false))
    override fun onBindViewHolder(h: Holder, pos: Int) = h.bind(getItem(pos))
}
