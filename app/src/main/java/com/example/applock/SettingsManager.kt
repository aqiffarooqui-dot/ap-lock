package com.example.applock

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {

    private const val PREF_NAME =
        "FarooquiAppLockPrefs"

    private const val KEY_BIOMETRIC_MODE =
        "biometric_mode"

    private const val KEY_UNLOCK_BEHAVIOR =
        "unlock_behavior"

    private const val KEY_INTRUDER_SELFIE =
        "intruder_selfie"

    // 0 = Biometric every time
    // 1 = Stay unlocked until phone is locked
    fun getBiometricMode(
        context: Context
    ): Int {

        val prefs: SharedPreferences =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        return prefs.getInt(
            KEY_BIOMETRIC_MODE,
            0
        )
    }

    fun setBiometricMode(
        context: Context,
        mode: Int
    ) {

        val prefs: SharedPreferences =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        prefs.edit()
            .putInt(
                KEY_BIOMETRIC_MODE,
                mode
            )
            .apply()
    }

    fun getUnlockBehavior(
        context: Context
    ): Int {

        val prefs: SharedPreferences =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        // Default:
        // Stay unlocked until phone is locked
        return prefs.getInt(
            KEY_UNLOCK_BEHAVIOR,
            1
        )
    }

    fun setUnlockBehavior(
        context: Context,
        behavior: Int
    ) {

        val prefs: SharedPreferences =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        prefs.edit()
            .putInt(
                KEY_UNLOCK_BEHAVIOR,
                behavior
            )
            .apply()
    }

    fun isIntruderSelfieEnabled(
        context: Context
    ): Boolean {

        val prefs: SharedPreferences =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        // Default = ON
        return prefs.getBoolean(
            KEY_INTRUDER_SELFIE,
            true
        )
    }

    fun setIntruderSelfieEnabled(
        context: Context,
        enabled: Boolean
    ) {

        val prefs: SharedPreferences =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        prefs.edit()
            .putBoolean(
                KEY_INTRUDER_SELFIE,
                enabled
            )
            .apply()
    }
}
