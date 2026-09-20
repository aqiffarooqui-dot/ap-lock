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

    private const val KEY_CALCULATOR_DISGUISE =
        "calculator_disguise"

    /*
     * Biometric mode:
     *
     * 0 = Ask biometric every time
     * 1 = Stay unlocked until phone is locked
     */
    fun getBiometricMode(
        context: Context
    ): Int {

        return getPrefs(context)
            .getInt(
                KEY_BIOMETRIC_MODE,
                0
            )
    }

    fun setBiometricMode(
        context: Context,
        mode: Int
    ) {

        getPrefs(context)
            .edit()
            .putInt(
                KEY_BIOMETRIC_MODE,
                mode
            )
            .apply()
    }

    /*
     * Unlock behavior:
     *
     * 0 = Clear unlock when leaving the app
     * 1 = Keep unlocked until screen lock
     */
    fun getUnlockBehavior(
        context: Context
    ): Int {

        return getPrefs(context)
            .getInt(
                KEY_UNLOCK_BEHAVIOR,
                1
            )
    }

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

    /*
     * Intruder selfie
     */
    fun isIntruderSelfieEnabled(
        context: Context
    ): Boolean {

        return getPrefs(context)
            .getBoolean(
                KEY_INTRUDER_SELFIE,
                true
            )
    }

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

    /*
     * Calculator disguise
     */
    fun isCalculatorDisguiseEnabled(
        context: Context
    ): Boolean {

        return getPrefs(context)
            .getBoolean(
                KEY_CALCULATOR_DISGUISE,
                false
            )
    }

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

    private fun getPrefs(
        context: Context
    ): SharedPreferences {

        return context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
    }
}
