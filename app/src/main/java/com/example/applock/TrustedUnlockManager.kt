package com.example.applock

import android.content.Context

object TrustedUnlockManager {

    private const val PREF_NAME =
        "FarooquiTrustedUnlock"

    private const val KEY_ENABLED =
        "trusted_unlock_enabled"

    private const val KEY_WIFI_ENABLED =
        "trusted_wifi_enabled"

    private const val KEY_WIFI_NAME =
        "trusted_wifi_name"

    private const val KEY_BLUETOOTH_ENABLED =
        "trusted_bluetooth_enabled"

    private const val KEY_BLUETOOTH_NAME =
        "trusted_bluetooth_name"

    private const val KEY_LOCATION_ENABLED =
        "trusted_location_enabled"

    private const val KEY_LOCATION_NAME =
        "trusted_location_name"

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

    fun isWifiEnabled(
        context: Context
    ): Boolean {
        return getPrefs(context)
            .getBoolean(
                KEY_WIFI_ENABLED,
                false
            )
    }

    fun setWifiEnabled(
        context: Context,
        enabled: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_WIFI_ENABLED,
                enabled
            )
            .apply()
    }

    fun getTrustedWifiName(
        context: Context
    ): String {
        return getPrefs(context)
            .getString(
                KEY_WIFI_NAME,
                ""
            )
            ?: ""
    }

    fun setTrustedWifiName(
        context: Context,
        name: String
    ) {
        getPrefs(context)
            .edit()
            .putString(
                KEY_WIFI_NAME,
                name.trim()
            )
            .apply()
    }

    fun isBluetoothEnabled(
        context: Context
    ): Boolean {
        return getPrefs(context)
            .getBoolean(
                KEY_BLUETOOTH_ENABLED,
                false
            )
    }

    fun setBluetoothEnabled(
        context: Context,
        enabled: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_BLUETOOTH_ENABLED,
                enabled
            )
            .apply()
    }

    fun getTrustedBluetoothName(
        context: Context
    ): String {
        return getPrefs(context)
            .getString(
                KEY_BLUETOOTH_NAME,
                ""
            )
            ?: ""
    }

    fun setTrustedBluetoothName(
        context: Context,
        name: String
    ) {
        getPrefs(context)
            .edit()
            .putString(
                KEY_BLUETOOTH_NAME,
                name.trim()
            )
            .apply()
    }

    fun isLocationEnabled(
        context: Context
    ): Boolean {
        return getPrefs(context)
            .getBoolean(
                KEY_LOCATION_ENABLED,
                false
            )
    }

    fun setLocationEnabled(
        context: Context,
        enabled: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_LOCATION_ENABLED,
                enabled
            )
            .apply()
    }

    fun getTrustedLocationName(
        context: Context
    ): String {
        return getPrefs(context)
            .getString(
                KEY_LOCATION_NAME,
                ""
            )
            ?: ""
    }

    fun setTrustedLocationName(
        context: Context,
        name: String
    ) {
        getPrefs(context)
            .edit()
            .putString(
                KEY_LOCATION_NAME,
                name.trim()
            )
            .apply()
    }

    fun hasConfiguredTrustedSource(
        context: Context
    ): Boolean {

        return (
            isWifiEnabled(context) &&
                    getTrustedWifiName(context)
                        .isNotBlank()
        ) || (
            isBluetoothEnabled(context) &&
                    getTrustedBluetoothName(context)
                        .isNotBlank()
        ) || (
            isLocationEnabled(context) &&
                    getTrustedLocationName(context)
                        .isNotBlank()
        )
    }

    fun shouldAllowUnlock(
        context: Context
    ): Boolean {

        if (!isEnabled(context)) {
            return false
        }

        /*
         * Actual Wi-Fi, Bluetooth and location
         * verification will be performed by the
         * final integration layer.
         *
         * This manager only stores the user's
         * trusted-unlock configuration.
         */
        return false
    }

    fun clearAll(
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
