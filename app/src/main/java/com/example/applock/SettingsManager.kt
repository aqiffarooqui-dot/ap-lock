package com.example.applock

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {

    private const val PREF_NAME = "FarooquiAppLockPrefs"

    private const val KEY_BIOMETRIC_MODE = "biometric_mode"
    private const val KEY_UNLOCK_BEHAVIOR = "unlock_behavior"
    private const val KEY_INTRUDER_SELFIE = "intruder_selfie"
    private const val KEY_CALCULATOR_DISGUISE = "calculator_disguise"

    // STEP 19 — Notification Privacy
    private const val KEY_NOTIFICATION_PRIVACY =
        "notification_privacy"

    fun getBiometricMode(context: Context): Int =
        getPrefs(context).getInt(
            KEY_BIOMETRIC_MODE,
            0
        )

    fun setBiometricMode(
        context: Context,
        mode: Int
    ) {
        getPrefs(context)
            .edit()
            .putInt(KEY_BIOMETRIC_MODE, mode)
            .apply()
    }

    fun getUnlockBehavior(context: Context): Int =
        getPrefs(context).getInt(
            KEY_UNLOCK_BEHAVIOR,
            1
        )

    fun setUnlockBehavior(
        context: Context,
        behavior: Int
    ) {
        getPrefs(context)
            .edit()
            .putInt(
                KEY_UNLOCK_BEHAVIOR,
                behavior
            )
            .apply()
    }

    fun isIntruderSelfieEnabled(
        context: Context
    ): Boolean =
        getPrefs(context).getBoolean(
            KEY_INTRUDER_SELFIE,
            true
        )

    fun setIntruderSelfieEnabled(
        context: Context,
        enabled: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_INTRUDER_SELFIE,
                enabled
            )
            .apply()
    }

    fun isCalculatorDisguiseEnabled(
        context: Context
    ): Boolean =
        getPrefs(context).getBoolean(
            KEY_CALCULATOR_DISGUISE,
            false
        )

    fun setCalculatorDisguiseEnabled(
        context: Context,
        enabled: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_CALCULATOR_DISGUISE,
                enabled
            )
            .apply()
    }

    // STEP 19 — Notification Privacy

    fun isNotificationPrivacyEnabled(
        context: Context
    ): Boolean =
        getPrefs(context).getBoolean(
            KEY_NOTIFICATION_PRIVACY,
            false
        )

    fun setNotificationPrivacyEnabled(
        context: Context,
        enabled: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_NOTIFICATION_PRIVACY,
                enabled
            )
            .apply()
    }

    private fun getPrefs(
        context: Context
    ): SharedPreferences =
        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
}
