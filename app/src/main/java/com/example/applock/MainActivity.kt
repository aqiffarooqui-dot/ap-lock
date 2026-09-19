package com.example.applock

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val lockedApps = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(android.graphics.Color.parseColor("#F8F9FA"))
        }

        val titleView = android.widget.TextView(this).apply {
            text = "Farooqui App Lock"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
            setPadding(0, 0, 0, 32)
        }
        layout.addView(titleView)

        val btnPermission = Button(this).apply {
            text = "1. Grant Usage Access Permission"
            setBackgroundColor(android.graphics.Color.parseColor("#007AFF"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }
        layout.addView(btnPermission)

        val btnBattery = Button(this).apply {
            text = "2. Disable Battery Optimization"
            setBackgroundColor(android.graphics.Color.parseColor("#34C759"))
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 16, 0, 16)
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
            }
        }
        layout.addView(btnBattery)

        val btnStartService = Button(this).apply {
            text = "3. Start Protection Service"
            setBackgroundColor(android.graphics.Color.parseColor("#FF9500"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                if (hasUsageStatsPermission()) {
                    val serviceIntent = Intent(this@MainActivity, AppLockService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                    Toast.makeText(this@MainActivity, "Protection Started Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Please grant Usage Access first!", Toast.LENGTH_LONG).show()
                }
            }
        }
        layout.addView(btnStartService)

        val listView = ListView(this).apply {
            setPadding(0, 24, 0, 0)
        }
        layout.addView(listView)

        setContentView(layout)
        loadInstalledApps(listView)
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun loadInstalledApps(listView: ListView) {
        val pm: PackageManager = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        val appList = mutableListOf<String>()
        val packageNames = mutableListOf<String>()

        for (app in packages) {
            if (app.flags and ApplicationInfo.FLAG_SYSTEM == 0 && app.packageName != packageName) {
                appList.add(pm.getApplicationLabel(app).toString())
                packageNames.add(app.packageName)
            }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, appList)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        listView.setOnItemClickListener { _, _, position, _ ->
            val pkg = packageNames[position]
            if (lockedApps.contains(pkg)) {
                lockedApps.remove(pkg)
                Toast.makeText(this, "Unlocked: ${appList[position]}", Toast.LENGTH_SHORT).show()
            } else {
                lockedApps.add(pkg)
                Toast.makeText(this, "Locked: ${appList[position]}", Toast.LENGTH_SHORT).show()
            }
            AppLockService.lockedAppsList = lockedApps
        }
    }
}
