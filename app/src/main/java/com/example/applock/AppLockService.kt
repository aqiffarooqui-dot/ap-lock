package com.example.applock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.util.*

class AppLockService : Service() {

    companion object {
        var lockedAppsList: Set<String> = emptySet()
        private const val CHANNEL_ID = "AppLockServiceChannel"
    }

    private var timer: Timer? = null
    private var lastLockedApp: String? = null
    private var unlockTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Farooqui App Lock")
            .setContentText("Protection service is running in background")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        timer?.cancel()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentApp = getForegroundApp()
                
                if (currentApp != null && currentApp != packageName) {
                    if (lockedAppsList.contains(currentApp)) {
                        // Agar same app dobara khula hai aur thodi der pehle hi unlock hua tha, toh bar-bar lock mat karo
                        if (currentApp != lastLockedApp || System.currentTimeMillis() - unlockTime > 4000) {
                            val lockIntent = Intent(applicationContext, LockScreenActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                putExtra("PACKAGE_NAME", currentApp)
                            }
                            startActivity(lockIntent)
                            lastLockedApp = currentApp
                        }
                    }
                }
            }
        }, 0, 500) // Har 0.5 seconds mein super-fast check
    }

    private fun getForegroundApp(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 5, time)
        if (!stats.isNullOrEmpty()) {
            val sortedMap = TreeMap<Long, android.app.usage.UsageStats>()
            for (usageStats in stats) {
                sortedMap[usageStats.lastTimeUsed] = usageStats
            }
            if (sortedMap.isNotEmpty()) {
                return sortedMap[sortedMap.lastKey()]?.packageName
            }
        }
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "AppLock Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        val restartIntent = Intent(applicationContext, AppLockService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent)
        } else {
            startService(restartIntent)
        }
    }
}
