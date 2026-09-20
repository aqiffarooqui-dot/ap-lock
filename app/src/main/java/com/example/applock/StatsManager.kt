package com.example.applock

import android.content.Context

object StatsManager {

    private const val PREF_NAME =
        "FarooquiAppLockStats"

    private const val KEY_UNLOCKS =
        "successful_unlocks"

    private const val KEY_FAILED_UNLOCKS =
        "failed_unlocks"

    private const val KEY_LOCKS =
        "app_locks"

    private const val KEY_SESSION_START =
        "session_start"

    private const val KEY_LAST_UNLOCK =
        "last_unlock"

    private const val KEY_LAST_LOCK =
        "last_lock"

    fun recordSuccessfulUnlock(
        context: Context
    ) {

        val prefs =
            getPrefs(context)

        prefs.edit()
            .putLong(
                KEY_UNLOCKS,
                prefs.getLong(
                    KEY_UNLOCKS,
                    0L
                ) + 1L
            )
            .putLong(
                KEY_LAST_UNLOCK,
                System.currentTimeMillis()
            )
            .apply()
    }

    fun recordFailedUnlock(
        context: Context
    ) {

        val prefs =
            getPrefs(context)

        prefs.edit()
            .putLong(
                KEY_FAILED_UNLOCKS,
                prefs.getLong(
                    KEY_FAILED_UNLOCKS,
                    0L
                ) + 1L
            )
            .apply()
    }

    fun recordAppLocked(
        context: Context
    ) {

        val prefs =
            getPrefs(context)

        prefs.edit()
            .putLong(
                KEY_LOCKS,
                prefs.getLong(
                    KEY_LOCKS,
                    0L
                ) + 1L
            )
            .putLong(
                KEY_LAST_LOCK,
                System.currentTimeMillis()
            )
            .apply()
    }

    fun startSession(
        context: Context
    ) {

        getPrefs(context)
            .edit()
            .putLong(
                KEY_SESSION_START,
                System.currentTimeMillis()
            )
            .apply()
    }

    fun getSuccessfulUnlocks(
        context: Context
    ): Long {

        return getPrefs(context)
            .getLong(
                KEY_UNLOCKS,
                0L
            )
    }

    fun getFailedUnlocks(
        context: Context
    ): Long {

        return getPrefs(context)
            .getLong(
                KEY_FAILED_UNLOCKS,
                0L
            )
    }

    fun getAppLocks(
        context: Context
    ): Long {

        return getPrefs(context)
            .getLong(
                KEY_LOCKS,
                0L
            )
    }

    fun getLastUnlockTime(
        context: Context
    ): Long {

        return getPrefs(context)
            .getLong(
                KEY_LAST_UNLOCK,
                0L
            )
    }

    fun getLastLockTime(
        context: Context
    ): Long {

        return getPrefs(context)
            .getLong(
                KEY_LAST_LOCK,
                0L
            )
    }

    fun getSessionStartTime(
        context: Context
    ): Long {

        return getPrefs(context)
            .getLong(
                KEY_SESSION_START,
                0L
            )
    }

    fun getTotalSecurityEvents(
        context: Context
    ): Long {

        return getSuccessfulUnlocks(context) +
                getFailedUnlocks(context) +
                IntrusionManager
                    .getTotalAttempts(context)
    }

    fun resetStats(
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
