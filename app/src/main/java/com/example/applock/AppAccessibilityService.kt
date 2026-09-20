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
    }

    private val screenReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                if (intent?.action ==
                    Intent.ACTION_SCREEN_OFF
                ) {

                    // Phone lock/screen off hone par
                    // saare temporary unlock sessions clear
                    clearAllSessions()

                    Log.d(
                        "AppLockService",
                        "All unlock sessions cleared"
                    )
                }
            }
        }

    override fun onServiceConnected() {
        super.onServiceConnected()

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

        val eventType = event.eventType

        // App/window change detect karo
        if (
            eventType !=
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType !=
            AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }

        val packageName =
            event.packageName?.toString()
                ?: return

        // Khud ke AppLock ko kabhi lock mat karo
        if (packageName == this.packageName) {
            return
        }

        // Agar app lock list me nahi hai
        // to kuch mat karo
        if (!lockedAppsList.contains(packageName)) {
            return
        }

        // Agar current session me already unlocked hai
        if (isSessionUnlocked(packageName)) {
            return
        }

        openLockScreen(packageName)
    }

    private fun openLockScreen(
        packageName: String
    ) {

        try {

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
                }

            startActivity(intent)

        } catch (e: Exception) {

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

        super.onDestroy()
    }
}
