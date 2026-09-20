package com.example.applock

import android.content.Context

object AppLockRulesManager {

    private const val PREF_NAME =
        "FarooquiAppLockRules"

    private const val KEY_AUTO_LOCK_NEW_APPS =
        "auto_lock_new_apps"

    private const val KEY_LOCK_SYSTEM_APPS =
        "lock_system_apps"

    private const val KEY_LOCK_ON_RESTART =
        "lock_on_restart"

    private const val KEY_PROTECT_RECENTS =
        "protect_recents"

    private const val KEY_BLOCK_LOCKED_NOTIFICATIONS =
        "block_locked_notifications"

    private const val KEY_FAILED_ATTEMPTS =
        "failed_attempts"

    fun isAutoLockNewAppsEnabled(
        context: Context
    ): Boolean {
        return getPrefs(context)
            .getBoolean(
                KEY_AUTO_LOCK_NEW_APPS,
                false
            )
    }

    fun setAutoLockNewAppsEnabled(
        context: Context,
        enabled: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_AUTO_LOCK_NEW_APPS,
                enabled
            )
            .apply()
    }

    fun isSystemAppLockEnabled(
        context: Context
    ): Boolean {
        return getPrefs(context)
            .getBoolean(
                KEY_LOCK_SYSTEM_APPS,
                true
            )
    }

    fun setSystemAppLockEnabled(
        context: Context,
        enabled: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_LOCK_SYSTEM_APPS,
                enabled
            )
            .apply()
    }

    fun isLockOnRestartEnabled(
        context: Context
    ): Boolean {
        return getPrefs(context)
            .getBoolean(
                KEY_LOCK_ON_RESTART,
                true
            )
    }

    fun setLockOnRestartEnabled(
        context: Context,
        enabled: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_LOCK_ON_RESTART,
                enabled
            )
            .apply()
    }

    fun isRecentAppsProtectionEnabled(
        context: Context
    ): Boolean {
        return getPrefs(context)
            .getBoolean(
                KEY_PROTECT_RECENTS,
                true
            )
    }

    fun setRecentAppsProtectionEnabled(
        context: Context,
        enabled: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_PROTECT_RECENTS,
                enabled
            )
            .apply()
    }

    fun isLockedNotificationBlockingEnabled(
        context: Context
    ): Boolean {
        return getPrefs(context)
            .getBoolean(
                KEY_BLOCK_LOCKED_NOTIFICATIONS,
                false
            )
    }

    fun setLockedNotificationBlockingEnabled(
        context: Context,
        enabled: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_BLOCK_LOCKED_NOTIFICATIONS,
                enabled
            )
            .apply()
    }

    fun getFailedAttemptLimit(
        context: Context
    ): Int {
        return getPrefs(context)
            .getInt(
                KEY_FAILED_ATTEMPTS,
                1
            )
            .coerceIn(1, 10)
    }

    fun setFailedAttemptLimit(
        context: Context,
        limit: Int
    ) {
        getPrefs(context)
            .edit()
            .putInt(
                KEY_FAILED_ATTEMPTS,
                limit.coerceIn(1, 10)
            )
            .apply()
    }

    fun resetToDefaults(
        context: Context
    ) {
        getPrefs(context)
            .edit()
            .clear()
            .apply()
    }

    private fun getPrefs(
        context: Context
    ) =
        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
}
