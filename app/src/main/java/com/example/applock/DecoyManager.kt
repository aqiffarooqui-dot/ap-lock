package com.example.applock

import android.content.Context

object DecoyManager {

    private const val PREF_NAME =
        "FarooquiDecoySettings"

    private const val KEY_ENABLED =
        "decoy_enabled"

    private const val KEY_MODE =
        "decoy_mode"

    const val MODE_NONE =
        "none"

    const val MODE_CALCULATOR =
        "calculator"

    const val MODE_FAKE_CRASH =
        "fake_crash"

    fun isEnabled(
        context: Context
    ): Boolean {
        return getPrefs(context)
            .getBoolean(
                KEY_ENABLED,
                false
            )
    }

    fun setEnabled(
        context: Context,
        enabled: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_ENABLED,
                enabled
            )
            .apply()
    }

    fun getMode(
        context: Context
    ): String {
        return getPrefs(context)
            .getString(
                KEY_MODE,
                MODE_NONE
            )
            ?: MODE_NONE
    }

    fun setMode(
        context: Context,
        mode: String
    ) {

        val validMode =
            when (mode) {
                MODE_CALCULATOR ->
                    MODE_CALCULATOR

                MODE_FAKE_CRASH ->
                    MODE_FAKE_CRASH

                else ->
                    MODE_NONE
            }

        getPrefs(context)
            .edit()
            .putString(
                KEY_MODE,
                validMode
            )
            .apply()
    }

    fun enableCalculator(
        context: Context
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_ENABLED,
                true
            )
            .putString(
                KEY_MODE,
                MODE_CALCULATOR
            )
            .apply()
    }

    fun enableFakeCrash(
        context: Context
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_ENABLED,
                true
            )
            .putString(
                KEY_MODE,
                MODE_FAKE_CRASH
            )
            .apply()
    }

    fun disable(
        context: Context
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_ENABLED,
                false
            )
            .putString(
                KEY_MODE,
                MODE_NONE
            )
            .apply()
    }

    fun isCalculatorMode(
        context: Context
    ): Boolean {
        return isEnabled(context) &&
                getMode(context) ==
                MODE_CALCULATOR
    }

    fun isFakeCrashMode(
        context: Context
    ): Boolean {
        return isEnabled(context) &&
                getMode(context) ==
                MODE_FAKE_CRASH
    }

    private fun getPrefs(
        context: Context
    ) =
        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
}
