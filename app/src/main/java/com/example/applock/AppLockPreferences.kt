package com.example.applock

import android.content.Context

object AppLockPreferences {

    private const val PREF_NAME = "FarooquiLockedApps"

    private const val KEY_LOCKED_APPS = "locked_apps"

    fun getLockedApps(context: Context): MutableSet<String> {

        val prefs = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )

        return prefs
            .getStringSet(
                KEY_LOCKED_APPS,
                emptySet()
            )
            ?.toMutableSet()
            ?: mutableSetOf()
    }

    fun saveLockedApps(
        context: Context,
        apps: Set<String>
    ) {

        val prefs = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .putStringSet(
                KEY_LOCKED_APPS,
                apps.toSet()
            )
            .apply()
    }

    fun isLocked(
        context: Context,
        packageName: String
    ): Boolean {

        return getLockedApps(context)
            .contains(packageName)
    }

    fun setLocked(
        context: Context,
        packageName: String,
        locked: Boolean
    ) {

        val apps =
            getLockedApps(context)

        if (locked) {
            apps.add(packageName)
        } else {
            apps.remove(packageName)
        }

        saveLockedApps(
            context,
            apps
        )
    }
}
