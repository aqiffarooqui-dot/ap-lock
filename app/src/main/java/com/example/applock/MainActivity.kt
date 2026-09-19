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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val lockedAppsSet = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(android.graphics.Color.parseColor("#F2F2F7")) // iOS Background color
        }

        val titleView = TextView(this).apply {
            text = "Farooqui App Lock"
            textSize = 26f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#000000"))
            setPadding(0, 16, 0, 24)
        }
        mainLayout.addView(titleView)

        // Permission Buttons Container
        val btnPermission = Button(this).apply {
            text = "1. Grant Usage Access Permission"
            setBackgroundColor(android.graphics.Color.parseColor("#007AFF"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }
        mainLayout.addView(btnPermission)

        val btnBattery = Button(this).apply {
            text = "2. Disable Battery Optimization"
            setBackgroundColor(android.graphics.Color.parseColor("#34C759"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
            }
        }
        mainLayout.addView(btnBattery)

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
                    Toast.makeText(this@MainActivity, "Protection Started!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Please grant Usage Access first!", Toast.LENGTH_LONG).show()
                }
            }
        }
        mainLayout.addView(btnStartService)

        val subTitle = TextView(this).apply {
            text = "Select Apps to Lock"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#3A3A3C"))
            setPadding(0, 32, 0, 16)
        }
        mainLayout.addView(subTitle)

        val listView = ListView(this)
        mainLayout.addView(listView)

        setContentView(mainLayout)
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
        
        val appList = mutableListOf<AppModel>()

        for (app in packages) {
            if (app.flags and ApplicationInfo.FLAG_SYSTEM == 0 && app.packageName != packageName) {
                val appName = pm.getApplicationLabel(app).toString()
                val icon = pm.getApplicationIcon(app)
                appList.add(AppModel(appName, app.packageName, icon, false))
            }
        }

        // Alphabetical sorting
        appList.sortBy { it.appName }

        val adapter = AppAdapter(this, appList) { app, isLocked ->
            if (isLocked) {
                lockedAppsSet.add(app.packageName)
                Toast.makeText(this, "${app.appName} Locked", Toast.LENGTH_SHORT).show()
            } else {
                lockedAppsSet.remove(app.packageName)
                Toast.makeText(this, "${app.appName} Unlocked", Toast.LENGTH_SHORT).show()
            }
            AppLockService.lockedAppsList = lockedAppsSet
        }

        listView.adapter = adapter
    }
}
