package com.example.applock

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.os.Build
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

                when (intent?.action) {

                    Intent.ACTION_SCREEN_OFF -> {

                        clearAllSessions()

                        context?.let {
                            TemporaryUnlockManager.clearAll(it)
                        }

                        lastForegroundPackage = null

                        Log.d(
                            "AppLockService",
                            "Screen locked - all sessions cleared"
                        )
                    }

                    Intent.ACTION_SCREEN_ON -> {

                        lastForegroundPackage = null

                        Log.d(
                            "AppLockService",
                            "Screen turned on"
                        )
                    }

                    Intent.ACTION_USER_UNLOCKED -> {

                        if (
                            AppLockRulesManager
                                .isLockOnRestartEnabled(
                                    this@AppAccessibilityService
                                )
                        ) {
                            clearAllSessions()

                            context?.let {
                                TemporaryUnlockManager.clearAll(it)
                            }
                        }

                        lastForegroundPackage = null

                        Log.d(
                            "AppLockService",
                            "Device unlocked - security sessions refreshed"
                        )
                    }
                }
            }
        }

    override fun onServiceConnected() {

        super.onServiceConnected()

        refreshLockedApps()

        /*
         * On service restart/reconnection, old authentication
         * sessions must not survive when Lock On Restart is enabled.
         */
        if (
            AppLockRulesManager
                .isLockOnRestartEnabled(this)
        ) {
            clearAllSessions()
            TemporaryUnlockManager.clearAll(this)
        }

        try {

            val filter =
                IntentFilter().apply {

                    addAction(
                        Intent.ACTION_SCREEN_OFF
                    )

                    addAction(
                        Intent.ACTION_SCREEN_ON
                    )

                    addAction(
                        Intent.ACTION_USER_UNLOCKED
                    )
                }

            if (Build.VERSION.SDK_INT >= 33) {

                registerReceiver(
                    screenReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
                )

            } else {

                @Suppress("DEPRECATION")
                registerReceiver(
                    screenReceiver,
                    filter
                )
            }

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

        /*
         * Never lock Farooqui App Lock itself.
         */
        if (
            packageName == this.packageName
        ) {
            return
        }

        refreshLockedApps()

        val previousPackage =
            lastForegroundPackage

        /*
         * MODE 0
         *
         * Ask biometric every time.
         *
         * When user leaves a protected app, its normal
         * authentication session is cleared.
         */
        if (
            previousPackage != null &&
            previousPackage != packageName &&
            SettingsManager.getUnlockBehavior(this) == 0
        ) {

            clearSession(previousPackage)

            Log.d(
                "AppLockService",
                "Normal session cleared: $previousPackage"
            )
        }

        lastForegroundPackage =
            packageName

        /*
         * Determine whether this package is protected.
         */
        val manuallyLocked =
            lockedAppsList.contains(packageName)

        val groupLocked =
            isLockedByGroup(packageName)

        val appIsLocked =
            manuallyLocked || groupLocked

        if (!appIsLocked) {
            return
        }

        /*
         * System application protection.
         *
         * If system-app locking is disabled, a system package
         * will not be intercepted even if it exists in the
         * locked-app list.
         */
        if (
            isSystemApplication(packageName) &&
            !AppLockRulesManager
                .isSystemAppLockEnabled(this)
        ) {

            clearSession(packageName)

            Log.d(
                "AppLockService",
                "System app protection disabled: $packageName"
            )

            return
        }

        /*
         * Scheduled App Lock.
         *
         * Schedule acts as an additional condition.
         */
        if (
            ScheduledLockManager
                .isEnabled(this)
        ) {

            val scheduleActive =
                ScheduledLockManager
                    .isInsideSchedule(this)

            if (!scheduleActive) {

                clearSession(packageName)

                Log.d(
                    "AppLockService",
                    "Outside scheduled protection window: $packageName"
                )

                return
            }
        }

        /*
         * Temporary unlock has highest priority.
         *
         * It remains valid until its configured expiry
         * or until the phone screen is locked.
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
                "Temporary unlock active: $packageName"
            )

            return
        }

        /*
         * Existing authenticated session.
         *
         * In "Stay unlocked until phone is locked" mode,
         * this survives app switching and is cleared only
         * when the phone screen is locked.
         */
        if (
            isSessionUnlocked(packageName)
        ) {

            return
        }

        /*
         * Prevent duplicate lock-screen activities.
         */
        if (
            isLockScreenActive(packageName)
        ) {

            return
        }

        openLockScreen(packageName)
    }

    private fun refreshLockedApps() {

        lockedAppsList =
            AppLockPreferences
                .getLockedApps(this)
                .toSet()
    }

    /*
     * Group protection is combined with the normal
     * AppLockPreferences list.
     *
     * A group contributes protection only when that
     * particular group is marked as locked.
     */
    private fun isLockedByGroup(
        packageName: String
    ): Boolean {

        val groups =
            AppGroupManager
                .getGroupsForApp(
                    this,
                    packageName
                )

        return groups.any { groupName ->
            AppGroupManager
                .isGroupLocked(
                    this,
                    groupName
                )
        }
    }

    private fun isSystemApplication(
        packageName: String
    ): Boolean {

        return try {

            val appInfo =
                packageManager.getApplicationInfo(
                    packageName,
                    0
                )

            val isSystem =
                (
                    appInfo.flags and
                        ApplicationInfo.FLAG_SYSTEM
                ) != 0

            val isUpdatedSystem =
                (
                    appInfo.flags and
                        ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
                ) != 0

            isSystem || isUpdatedSystem

        } catch (_: Exception) {

            false
        }
    }

    private fun getApplicationDisplayName(
        packageName: String
    ): String {

        return try {

            val appInfo =
                packageManager.getApplicationInfo(
                    packageName,
                    0
                )

            packageManager
                .getApplicationLabel(
                    appInfo
                )
                .toString()

        } catch (_: Exception) {

            packageName
        }
    }

    private fun openLockScreen(
        packageName: String
    ) {

        try {

            setLockScreenActive(
                packageName
            )

            val appName =
                getApplicationDisplayName(
                    packageName
                )

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

            startActivity(intent)

            Log.d(
                "AppLockService",
                "Lock screen opened for: $appName"
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
        // AccessibilityService requirement.
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
