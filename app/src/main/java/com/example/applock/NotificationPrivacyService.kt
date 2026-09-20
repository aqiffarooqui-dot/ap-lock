package com.example.applock

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationPrivacyService : NotificationListenerService() {

    companion object {

        private var serviceInstance: NotificationPrivacyService? = null

        fun refreshLockedApps() {
            serviceInstance?.refreshExistingNotifications()
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        serviceInstance = this

        Log.d(
            "NotificationPrivacy",
            "Notification listener connected"
        )

        refreshExistingNotifications()
    }

    override fun onListenerDisconnected() {
        serviceInstance = null
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(
        sbn: StatusBarNotification?
    ) {

        if (sbn == null) {
            return
        }

        hideIfRequired(sbn)
    }

    private fun refreshExistingNotifications() {

        if (!SettingsManager.isNotificationPrivacyEnabled(this)) {
            return
        }

        try {

            val activeNotifications =
                activeNotifications ?: return

            for (notification in activeNotifications) {
                hideIfRequired(notification)
            }

        } catch (e: Exception) {

            Log.e(
                "NotificationPrivacy",
                "Unable to refresh notifications",
                e
            )
        }
    }

    private fun hideIfRequired(
        sbn: StatusBarNotification
    ) {

        if (
            !SettingsManager
                .isNotificationPrivacyEnabled(this)
        ) {
            return
        }

        val packageName =
            sbn.packageName

        // Never hide Farooqui App Lock's own notifications.
        if (packageName == this.packageName) {
            return
        }

        if (
            !AppLockPreferences
                .isLocked(
                    this,
                    packageName
                )
        ) {
            return
        }

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

    override fun onDestroy() {

        if (serviceInstance === this) {
            serviceInstance = null
        }

        super.onDestroy()
    }
}
