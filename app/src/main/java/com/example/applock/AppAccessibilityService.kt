package com.example.applock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AppAccessibilityService : AccessibilityService() {

    companion object {
        var lockedAppsList: Set<String> = emptySet()

        private val unlockedSessions = mutableSetOf<String>()

        fun setSessionUnlocked(packageName: String) {
            unlockedSessions.add(packageName)
        }

        fun isCurrentlyLocked(packageName: String): Boolean {
            return lockedAppsList.contains(packageName) &&
                    !unlockedSessions.contains(packageName)
        }

        fun clearSession(packageName: String) {
            unlockedSessions.remove(packageName)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // Apne app ko dobara lock screen par mat bhejo
            if (packageName == this.packageName) {
                return
            }

            if (isCurrentlyLocked(packageName)) {

                // Session ko clear karo jab locked app dobara open ho
                clearSession(packageName)

                val intent = Intent(this, LockScreenActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("PACKAGE_NAME", packageName)
                }

                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(
                        "AppLockService",
                        "Error launching lock screen",
                        e
                    )
                }
            }
        }
    }

    override fun onInterrupt() {
        // Required override
    }
}
