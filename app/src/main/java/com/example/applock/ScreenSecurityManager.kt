package com.example.applock

import android.app.Activity
import android.view.WindowManager

object ScreenSecurityManager {

    private const val PREF_NAME =
        "FarooquiScreenSecurity"

    private const val KEY_ENABLED =
        "screen_security_enabled"

    fun isEnabled(
        context: android.content.Context
    ): Boolean {
        return context
            .getSharedPreferences(
                PREF_NAME,
                android.content.Context.MODE_PRIVATE
            )
            .getBoolean(
                KEY_ENABLED,
                true
            )
    }

    fun setEnabled(
        context: android.content.Context,
        enabled: Boolean
    ) {
        context
            .getSharedPreferences(
                PREF_NAME,
                android.content.Context.MODE_PRIVATE
            )
            .edit()
            .putBoolean(
                KEY_ENABLED,
                enabled
            )
            .apply()
    }

    fun apply(
        activity: Activity
    ) {
        if (isEnabled(activity)) {
            activity.window.addFlags(
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            activity.window.clearFlags(
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }

    fun enable(
        activity: Activity
    ) {
        activity.window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun disable(
        activity: Activity
    ) {
        activity.window.clearFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
}
