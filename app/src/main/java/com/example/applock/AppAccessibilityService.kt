package com.example.applock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppAccessibilityService : AccessibilityService() {

    companion object {
        var lockedAppsList: MutableSet<String> = mutableSetOf()
        var isCurrentlyLocked: Boolean = false // Track karega ki abhi lock screen active hai ya nahi
        private var lastLockedApp: String? = null
        private var lastUnlockTime: Long = 0
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
                    // Agar pehle se lock screen open nahi hai, tab hi naya intent bhejo
                    if (!isCurrentlyLocked) {
                        if (packageName != lastLockedApp || System.currentTimeMillis() - lastUnlockTime > 4000) {
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
    }

    override fun onInterrupt() {}
}
