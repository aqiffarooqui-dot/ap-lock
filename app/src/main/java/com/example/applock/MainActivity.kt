package com.example.applock

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
            setBackgroundColor(android.graphics.Color.parseColor("#F2F2F7"))
        }

        val titleView = TextView(this).apply {
            text = "Farooqui App Lock"
            textSize = 26f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#000000"))
            setPadding(0, 16, 0, 24)
        }
        mainLayout.addView(titleView)

        val btnAccessibility = Button(this).apply {
            text = "Enable Accessibility Protection"
            setBackgroundColor(android.graphics.Color.parseColor("#007AFF"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                Toast.makeText(context, "Find 'Farooqui App Lock' and turn it ON", Toast.LENGTH_LONG).show()
            }
        }
        mainLayout.addView(btnAccessibility)

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

        appList.sortBy { it.appName }

        val adapter = AppAdapter(this, appList) { app, isLocked ->
            if (isLocked) {
                lockedAppsSet.add(app.packageName)
                Toast.makeText(this, "${app.appName} Locked", Toast.LENGTH_SHORT).show()
            } else {
                lockedAppsSet.remove(app.packageName)
                Toast.makeText(this, "${app.appName} Unlocked", Toast.LENGTH_SHORT).show()
            }
            AppAccessibilityService.lockedAppsList = lockedAppsSet
        }

        listView.adapter = adapter
    }
}
