package com.example.applock

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREF_NAME = "FarooquiAppLockPrefs"
    private const val KEY_BIOMETRIC_MODE = "biometric_mode" // 0 = Both, 1 = Fingerprint Only, 2 = Face Only

    fun getBiometricMode(context: Context): Int {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_BIOMETRIC_MODE, 0)
    }

    fun setBiometricMode(context: Context, mode: Int) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_BIOMETRIC_MODE, mode).apply()
    }
}
