package com.example.applock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppAccessibilityService : AccessibilityService() {

    companion object {
        var lockedAppsList: MutableSet<String> = mutableSetOf()
        var isCurrentlyLocked: Boolean = false
        private var lastLockedApp: String? = null
        private var lastUnlockTime: Long = 0
        
        // Cooldown map taaki same app baar-bar lock na ho turant unlock hone ke baad
        private val unlockSessions = mutableMapOf<String, Long>()

        fun isSessionActive(packageName: String): Boolean {
            val unlockTime = unlockSessions[packageName] ?: 0L
            return System.currentTimeMillis() - unlockTime < 5000 // 5 seconds cooldown window
        }

        fun clearSession(packageName: String) {
            unlockSessions.remove(packageName)
        }

        fun setSessionUnlocked(packageName: String) {
            unlockSessions[packageName] = System.currentTimeMillis()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            val packageName = event.packageName?.toString() ?: return

            if (packageName != "com.example.applock" && 
                packageName != "com.android.systemui" && 
                packageName != "android") {
                
                if (lockedAppsList.contains(packageName)) {
                    // Agar session active nahi hai aur abhi lock screen open nahi hai, tab hi trigger karo
                    if (!isCurrentlyLocked && !isSessionActive(packageName)) {
                        isCurrentlyLocked = true
                        
                        val lockIntent = Intent(applicationContext, LockScreenActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            putExtra("PACKAGE_NAME", packageName)
                        }
                        startActivity(lockIntent)
                        lastLockedApp = packageName
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        isCurrentlyLocked = false
    }
}
