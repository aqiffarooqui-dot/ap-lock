package com.example.applock

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AppAccessibilityService : AccessibilityService() {

    companion object {

        var lockedAppsList: Set<String> = emptySet()

        private val unlockedSessions = mutableSetOf<String>()

        private var activeLockScreenPackage: String? = null

        fun setSessionUnlocked(packageName: String) {
            unlockedSessions.add(packageName)
        }

        fun isSessionUnlocked(packageName: String): Boolean {
            return unlockedSessions.contains(packageName)
        }

        fun clearSession(packageName: String) {
            unlockedSessions.remove(packageName)
        }

        fun clearAllSessions() {
            unlockedSessions.clear()
        }

        fun setLockScreenActive(packageName: String) {
            activeLockScreenPackage = packageName
        }

        fun clearLockScreenActive(packageName: String?) {
            if (
                packageName == null ||
                activeLockScreenPackage == packageName
            ) {
                activeLockScreenPackage = null
            }
        }

        fun isLockScreenActive(packageName: String): Boolean {
            return activeLockScreenPackage == packageName
        }
    }

    private var lastForegroundPackage: String? = null

    private val screenReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                when (intent?.action) {

                    Intent.ACTION_SCREEN_OFF -> {

                        // Mode 0 and Mode 1 both require
                        // authentication again after phone lock.
                        clearAllSessions()

                        lastForegroundPackage = null

                        Log.d(
                            "AppLockService",
                            "Screen locked - all sessions cleared"
                        )
                    }

                    Intent.ACTION_SCREEN_ON -> {

                        lastForegroundPackage = null

                        Log.d(
                            "AppLockService",
                            "Screen turned on"
                        )
                    }
                }
            }
        }

    override fun onServiceConnected() {

        super.onServiceConnected()

        lockedAppsList =
            AppLockPreferences
                .getLockedApps(this)
                .toSet()

        try {

            val filter =
                IntentFilter().apply {

                    addAction(
                        Intent.ACTION_SCREEN_OFF
                    )

                    addAction(
                        Intent.ACTION_SCREEN_ON
                    )
                }

            registerReceiver(
                screenReceiver,
                filter
            )

        } catch (e: Exception) {

            Log.e(
                "AppLockService",
                "Receiver registration failed",
                e
            )
        }

        Log.d(
            "AppLockService",
            "Accessibility service connected"
        )
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        if (event == null) {
            return
        }

        val eventType = event.eventType

        if (
            eventType !=
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType !=
            AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }

        val packageName =
            event.packageName
                ?.toString()
                ?: return

        // Ignore Farooqui App Lock itself.
        if (packageName == this.packageName) {
            return
        }

        lockedAppsList =
            AppLockPreferences
                .getLockedApps(this)
                .toSet()

        val previousPackage =
            lastForegroundPackage

        /*
         * Mode 0:
         * "Ask biometric every time"
         *
         * When user leaves an unlocked application,
         * its temporary session is cleared.
         */
        if (
            previousPackage != null &&
            previousPackage != packageName &&
            SettingsManager.getUnlockBehavior(this) == 0
        ) {

            clearSession(previousPackage)

            Log.d(
                "AppLockService",
                "Cleared session for: $previousPackage"
            )
        }

        lastForegroundPackage = packageName

        // Current app is not locked.
        if (!lockedAppsList.contains(packageName)) {
            return
        }

        // Already authenticated for this session.
        if (isSessionUnlocked(packageName)) {
            return
        }

        // Lock screen is already visible.
        if (isLockScreenActive(packageName)) {
            return
        }

        openLockScreen(packageName)
    }

    private fun openLockScreen(
        packageName: String
    ) {

        try {

            setLockScreenActive(packageName)

            val appName =
                try {

                    val appInfo =
                        packageManager.getApplicationInfo(
                            packageName,
                            0
                        )

                    packageManager
                        .getApplicationLabel(appInfo)
                        .toString()

                } catch (e: Exception) {

                    packageName
                }

            val intent =
                Intent(
                    this,
                    LockScreenActivity::class.java
                ).apply {

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )

                    putExtra(
                        "PACKAGE_NAME",
                        packageName
                    )

                    putExtra(
                        "APP_NAME",
                        appName
                    )
                }

            startActivity(intent)

            Log.d(
                "AppLockService",
                "Lock screen opened for: $appName"
            )

        } catch (e: Exception) {

            clearLockScreenActive(packageName)

            Log.e(
                "AppLockService",
                "Unable to open lock screen",
                e
            )
        }
    }

    override fun onInterrupt() {
        // Required by AccessibilityService.
    }

    override fun onDestroy() {

        try {

            unregisterReceiver(
                screenReceiver
            )

        } catch (_: Exception) {
            // Receiver may already be unregistered.
        }

        clearAllSessions()

        activeLockScreenPackage = null

        lastForegroundPackage = null

        super.onDestroy()
    }
}
