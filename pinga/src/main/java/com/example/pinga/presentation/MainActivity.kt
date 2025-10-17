package com.example.pinga.presentation

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pinga.R
import com.example.pinga.core.Permissions
import com.example.pinga.data.Repo
import com.example.pinga.export.Export
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val adapter = DeviceAdapter()
    private val repo by lazy { Repo(this) }

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permLauncher.launch(Permissions.all())

        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop  = findViewById<Button>(R.id.btnStop)
        val btnExport= findViewById<Button>(R.id.btnExport)

        btnStart.setOnClickListener {
            repo.start()
            btnStart.isEnabled = false
            btnStop.isEnabled  = true
        }
        btnStop.setOnClickListener {
            repo.stop()
            btnStart.isEnabled = true
            btnStop.isEnabled  = false
        }
        btnExport.setOnClickListener {
            val snap = repo.buildSnapshot()
            val json = Export.toJson(snap)

            val file = java.io.File(cacheDir, "pinga_export_${System.currentTimeMillis()}.json")
            file.writeText(json.toString(2))

            val uri = androidx.core.content.FileProvider.getUriForFile(
                this, "${packageName}.provider", file
            )
            val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(android.content.Intent.createChooser(send, "Share export"))
        }

        lifecycleScope.launch {
            repo.rows.collectLatest { list -> adapter.submitList(list) }
        }
    }
}
