package com.example.applock

import android.content.Context

object TemporaryUnlockManager {

    private const val PREF_NAME =
        "FarooquiTemporaryUnlocks"

    private const val KEY_PREFIX =
        "temporary_unlock_"

    private const val UNTIL_SCREEN_LOCK =
        Long.MAX_VALUE

    fun setTemporaryUnlock(
        context: Context,
        packageName: String,
        durationMinutes: Int
    ) {

        val expiryTime =
            if (durationMinutes <= 0) {
                UNTIL_SCREEN_LOCK
            } else {
                System.currentTimeMillis() +
                        (durationMinutes * 60_000L)
            }

        context
            .getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putLong(
                KEY_PREFIX + packageName,
                expiryTime
            )
            .apply()
    }

    fun isTemporarilyUnlocked(
        context: Context,
        packageName: String
    ): Boolean {

        val prefs =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        val expiryTime =
            prefs.getLong(
                KEY_PREFIX + packageName,
                0L
            )

        if (expiryTime == 0L) {
            return false
        }

        if (
            expiryTime == UNTIL_SCREEN_LOCK ||
            System.currentTimeMillis() < expiryTime
        ) {
            return true
        }

        clearTemporaryUnlock(
            context,
            packageName
        )

        return false
    }

    fun getRemainingMinutes(
        context: Context,
        packageName: String
    ): Int {

        val prefs =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        val expiryTime =
            prefs.getLong(
                KEY_PREFIX + packageName,
                0L
            )

        if (expiryTime == 0L) {
            return 0
        }

        if (expiryTime == UNTIL_SCREEN_LOCK) {
            return -1
        }

        val remaining =
            expiryTime -
                    System.currentTimeMillis()

        if (remaining <= 0L) {
            clearTemporaryUnlock(
                context,
                packageName
            )
            return 0
        }

        return (
            (remaining + 59_999L) /
                    60_000L
            ).toInt()
    }

    fun clearTemporaryUnlock(
        context: Context,
        packageName: String
    ) {

        context
            .getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .remove(
                KEY_PREFIX + packageName
            )
            .apply()
    }

    fun clearAll(
        context: Context
    ) {

        val prefs =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        prefs.edit()
            .clear()
            .apply()
    }
}
