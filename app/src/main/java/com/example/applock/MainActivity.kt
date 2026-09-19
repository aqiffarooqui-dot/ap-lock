package com.example.applock

import android.app.AppOpsManager
import android.content.Context
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

class MainActivity : AppCompatActivity() {

    private val lockedApps = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val btnPermission = Button(this).apply {
            text = "1. Grant Usage Access Permission"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }
        layout.addView(btnPermission)

        val btnStartService = Button(this).apply {
            text = "2. Start AppLock Service"
            setOnClickListener {
                if (hasUsageStatsPermission()) {
                    val serviceIntent = Intent(this@MainActivity, AppLockService::class.java)
                    startForegroundService(serviceIntent)
                    Toast.makeText(this@MainActivity, "AppLock Service Started!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Please grant Usage Access permission first!", Toast.LENGTH_LONG).show()
                }
            }
        }
        layout.addView(btnStartService)

        val listView = ListView(this)
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
            // Save locked apps list to static memory for service access
            AppLockService.lockedAppsList = lockedApps
        }
    }
}
