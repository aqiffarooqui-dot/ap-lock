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
        var lockedAppsList: MutableSet<String> = mutableSetOf()
        private const val CHANNEL_ID = "FarooquiAppLockChannel"
    }

    private var timer: Timer? = null
    private var lastLockedApp: String? = null
    private var lastUnlockTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Farooqui App Lock Active")
            .setContentText("Protecting your private apps securely")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        startForeground(101, notification)
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        timer?.cancel()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentApp = getForegroundApp()
                
                if (currentApp != null && currentApp != packageName && currentApp != "com.android.systemui") {
                    if (lockedAppsList.contains(currentApp)) {
                        // Agar app lock hai aur pichle 4 seconds mein unlock nahi hua
                        if (currentApp != lastLockedApp || System.currentTimeMillis() - lastUnlockTime > 4000) {
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
        }, 0, 300) // Har 0.3 seconds mein lightning-fast monitoring
    }

    private fun getForegroundApp(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        // Query last 3 seconds
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 3000, time)
        if (!stats.isNullOrEmpty()) {
            var recentPkg: String? = null
            var maxTime: Long = 0
            for (usageStats in stats) {
                if (usageStats.lastTimeUsed > maxTime) {
                    maxTime = usageStats.lastTimeUsed
                    recentPkg = usageStats.packageName
                }
            }
            return recentPkg
        }
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "AppLock Background Protection",
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
