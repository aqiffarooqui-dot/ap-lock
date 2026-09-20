package com.example.applock

accessibilityservice.AccessibilityService
view.accessibility.AccessibilityNodeInfo
content.Intent
util.Log
view.accessibility.AccessibilityEvent

class AppAccessibilityService : AccessibilityService() {

    companion object {
        var lockedAppsList: Set<String> = mutableSetOf()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // Check karo ki app locked list mein hai ya nahi
            if (lockedAppsList.contains(packageName)) {
                // Agar locked hai, toh Lock Activity launch karo
                val intent = Intent(this, LockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("locked_package", packageName)
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("AppLockService", "Error launching lock screen: ${e.message}")
                }
            }
        }
    }

    override fun onInterrupt() {
        // Required override
    }
}
