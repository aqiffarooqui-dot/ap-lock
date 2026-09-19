package com.example.applock

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import java.util.*

class AppLockService : Service() {

    companion object {
        var lockedAppsList: Set<String> = emptySet()
    }

    private var timer: Timer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        timer?.cancel()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentApp = getForegroundApp()
                if (currentApp != null && lockedAppsList.contains(currentApp)) {
                    // Agar app locked list mein hai toh LockScreen Activity khol do
                    val lockIntent = Intent(applicationContext, LockScreenActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        putExtra("PACKAGE_NAME", currentApp)
                    }
                    startActivity(lockIntent)
                }
            }
        }, 0, 1000) // Har 1 second mein check karega
    }

    private fun getForegroundApp(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
        if (stats != null && stats.isNotEmpty()) {
            val sortedMap = TreeMap<Long, android.app.usage.UsageStats>()
            for (usageStats in stats) {
                sortedMap[usageStats.lastTimeUsed] = usageStats
            }
            if (!sortedMap.isEmpty()) {
                return sortedMap[sortedMap.lastKey()]?.packageName
            }
        }
        return null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
