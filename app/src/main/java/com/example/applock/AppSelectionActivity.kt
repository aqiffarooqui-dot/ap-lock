package com.example.applock

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AppSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Simple ListView layout create kar rahe hain code ke andar hi taaki alag se XML na banani pade
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val btnPermission = Button(this).apply {
            text = "Grant Usage Stats Permission"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }
        layout.addView(btnPermission)

        val listView = ListView(this)
        layout.addView(listView)

        setContentView(layout)

        // Installed apps load karna
        loadInstalledApps(listView)
    }

    private fun loadInstalledApps(listView: ListView) {
        val pm: PackageManager = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        val appNames = mutableListOf<String>()
        for (app in packages) {
            // Sirf user apps dikhane ke liye (system apps chhod kar)
            if (app.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                val appName = pm.getApplicationLabel(app).toString()
                appNames.add(appName)
            }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, appNames)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedApp = appNames[position]
            Toast.makeText(this, "Locked: $selectedApp", Toast.LENGTH_SHORT).show()
        }
    }
}
