package com.example.applock

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationPrivacyService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        if (!SettingsManager.isNotificationPrivacyEnabled(this)) {
            return
        }

        val packageName = sbn.packageName

        if (AppLockPreferences.isLocked(this, packageName)) {
            try {
                cancelNotification(sbn.key)

                Log.d(
                    "NotificationPrivacy",
                    "Hidden notification from: $packageName"
                )
            } catch (e: Exception) {
                Log.e(
                    "NotificationPrivacy",
                    "Unable to hide notification",
                    e
                )
            }
        }
    }
}
