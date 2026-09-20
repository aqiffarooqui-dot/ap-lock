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

        fun setSessionUnlocked(
            packageName: String
        ) {
            unlockedSessions.add(packageName)
        }

        fun isSessionUnlocked(
            packageName: String
        ): Boolean {
            return unlockedSessions.contains(
                packageName
            )
        }

        fun clearSession(
            packageName: String
        ) {
            unlockedSessions.remove(packageName)
        }

        fun clearAllSessions() {
            unlockedSessions.clear()
        }

        fun setLockScreenActive(
            packageName: String
        ) {
            activeLockScreenPackage =
                packageName
        }

        fun clearLockScreenActive(
            packageName: String?
        ) {
            if (
                packageName == null ||
                activeLockScreenPackage == packageName
            ) {
                activeLockScreenPackage = null
            }
        }

        fun isLockScreenActive(
            packageName: String
        ): Boolean {
            return activeLockScreenPackage ==
                    packageName
        }
    }

    private var lastForegroundPackage: String? =
        null

    private val screenReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                when (intent?.action) {

                    Intent.ACTION_SCREEN_OFF -> {

                        clearAllSessions()

                        context?.let {
                            TemporaryUnlockManager.clearAll(
                                it
                            )
                        }

                        lastForegroundPackage = null

                        Log.d(
                            "AppLockService",
                            "Screen locked - sessions and temporary unlocks cleared"
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
         * Ask biometric every time.
         */
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

            Log.d(
                "AppLockService",
                "Cleared normal session for: $previousPackage"
            )
        }

        lastForegroundPackage =
            packageName

        /*
         * Scheduled App Lock
         *
         * If scheduling is enabled and the current
         * time is inside the configured protection
         * window, the app is protected.
         *
         * Outside the schedule, normal manually
         * locked apps continue to work normally.
         */
        val manuallyLocked =
            lockedAppsList.contains(
                packageName
            )

        val scheduleActive =
            ScheduledLockManager.isInsideSchedule(
                this
            )

        /*
         * At the moment scheduled mode acts as an
         * additional protection layer for apps that
         * are already selected as locked.
         *
         * This keeps the user's manual app selection
         * as the source of truth and avoids locking
         * every installed app unexpectedly.
         */
        if (!manuallyLocked) {
            return
        }

        /*
         * Temporary unlock has priority.
         */
        if (
            TemporaryUnlockManager
                .isTemporarilyUnlocked(
                    this,
                    packageName
                )
        ) {

            Log.d(
                "AppLockService",
                "Temporary unlock active for: $packageName"
            )

            return
        }

        /*
         * If scheduled protection is active, the app
         * must be authenticated unless a valid session
         * or temporary unlock exists.
         *
         * Outside the schedule, manually locked apps
         * are still protected as usual.
         */
        if (scheduleActive) {

            Log.d(
                "AppLockService",
                "Scheduled protection active for: $packageName"
            )
        }

        if (
            isSessionUnlocked(
                packageName
            )
        ) {
            return
        }

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

            val appName =
                try {

                    val appInfo =
                        packageManager
                            .getApplicationInfo(
                                packageName,
                                0
                            )

                    packageManager
                        .getApplicationLabel(
                            appInfo
                        )
                        .toString()

                } catch (
                    e: Exception
                ) {

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

            Log.d(
                "AppLockService",
                "Lock screen opened for: $appName"
            )

        } catch (
            e: Exception
        ) {

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
        // Required by AccessibilityService.
    }

    override fun onDestroy() {

        try {
            unregisterReceiver(
                screenReceiver
            )
        } catch (_: Exception) {
        }

        clearAllSessions()

        activeLockScreenPackage = null

        lastForegroundPackage = null

        super.onDestroy()
    }
}
