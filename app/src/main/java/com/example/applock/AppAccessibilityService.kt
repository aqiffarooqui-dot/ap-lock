package com.example.applock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppAccessibilityService : AccessibilityService() {

    companion object {
        var lockedAppsList: MutableSet<String> = mutableSetOf()
        private var lastLockedApp: String? = null
        private var lastUnlockTime: Long = 0
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            if (packageName != "com.example.applock" && packageName != "com.android.systemui") {
                if (lockedAppsList.contains(packageName)) {
                    if (packageName != lastLockedApp || System.currentTimeMillis() - lastUnlockTime > 4000) {
                        val lockIntent = Intent(applicationContext, LockScreenActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            putExtra("PACKAGE_NAME", packageName)
                        }
                        startActivity(lockIntent)
                        lastLockedApp = packageName
                    }
                }
            }
        }
    }

    override fun onInterrupt() {}
}
