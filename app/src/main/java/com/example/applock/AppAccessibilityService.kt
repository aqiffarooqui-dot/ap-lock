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

        private val unlockedSessions =
            mutableSetOf<String>()

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

                if (
                    intent?.action ==
                    Intent.ACTION_SCREEN_OFF
                ) {

                    // Phone lock/screen off hone par
                    // temporary unlock sessions clear.
                    clearAllSessions()

                    lastForegroundPackage = null

                    Log.d(
                        "AppLockService",
                        "All unlock sessions cleared"
                    )
                }
            }
        }

    override fun onServiceConnected() {

        super.onServiceConnected()

        // Service restart hone ke baad bhi
        // saved locked apps load rahen.
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
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        if (event == null) {
            return
        }

        val eventType =
            event.eventType

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

        // Apne App Lock ke events ignore karo.
        if (
            packageName ==
            this.packageName
        ) {

            return
        }

        // Har event par persistent list refresh karo.
        lockedAppsList =
            AppLockPreferences
                .getLockedApps(this)
                .toSet()

        /*
         * MODE 0:
         * Ask biometric every time.
         *
         * Is mode me app foreground se bahar
         * jaate hi uska temporary session clear.
         *
         * MODE 1:
         * Stay unlocked until phone is locked.
         *
         * Is mode me app switch/minimize par
         * session clear nahi hoga.
         */

        val previousPackage =
            lastForegroundPackage

        if (
            previousPackage != null &&
            previousPackage != packageName &&
            SettingsManager.getUnlockBehavior(
                this
            ) == 0
        ) {

            clearSession(
                previousPackage
            )
        }

        lastForegroundPackage =
            packageName

        if (
            !lockedAppsList.contains(
                packageName
            )
        ) {

            return
        }

        // Already authenticated app.
        if (
            isSessionUnlocked(
                packageName
            )
        ) {

            return
        }

        /*
         * Accessibility multiple events bhej sakta hai.
         * Same app ke liye multiple biometric screens
         * mat kholo.
         */
        if (
            isLockScreenActive(
                packageName
            )
        ) {

            return
        }

        openLockScreen(
            packageName
        )
    }

    private fun openLockScreen(
        packageName: String
    ) {

        try {

            setLockScreenActive(
                packageName
            )

            val appInfo =
                try {

                    packageManager
                        .getApplicationInfo(
                            packageName,
                            0
                        )

                } catch (e: Exception) {

                    null
                }

            val appName =
                if (appInfo != null) {

                    packageManager
                        .getApplicationLabel(
                            appInfo
                        )
                        .toString()

                } else {

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

            startActivity(
                intent
            )

        } catch (e: Exception) {

            clearLockScreenActive(
                packageName
            )

            Log.e(
                "AppLockService",
                "Unable to open lock screen",
                e
            )
        }
    }

    override fun onInterrupt() {
        // Required by AccessibilityService
    }

    override fun onDestroy() {

        try {

            unregisterReceiver(
                screenReceiver
            )

        } catch (e: Exception) {
            // Ignore
        }

        clearAllSessions()

        activeLockScreenPackage = null

        lastForegroundPackage = null

        super.onDestroy()
    }
}
