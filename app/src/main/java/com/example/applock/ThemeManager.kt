package com.example.applock

import androidx.appcompat.app.AppCompatDelegate
import android.content.Context

object ThemeManager {

    private const val PREF_NAME =
        "FarooquiThemeSettings"

    private const val KEY_THEME =
        "theme_mode"

    const val THEME_SYSTEM =
        "system"

    const val THEME_LIGHT =
        "light"

    const val THEME_DARK =
        "dark"

    fun getTheme(
        context: Context
    ): String {

        return getPrefs(context)
            .getString(
                KEY_THEME,
                THEME_SYSTEM
            )
            ?: THEME_SYSTEM
    }

    fun setTheme(
        context: Context,
        theme: String
    ) {

        val validTheme =
            when (theme) {

                THEME_LIGHT ->
                    THEME_LIGHT

                THEME_DARK ->
                    THEME_DARK

                else ->
                    THEME_SYSTEM
            }

        getPrefs(context)
            .edit()
            .putString(
                KEY_THEME,
                validTheme
            )
            .apply()

        applyTheme(
            validTheme
        )
    }

    fun applySavedTheme(
        context: Context
    ) {

        applyTheme(
            getTheme(context)
        )
    }

    private fun applyTheme(
        theme: String
    ) {

        when (theme) {

            THEME_LIGHT -> {
                AppCompatDelegate
                    .setDefaultNightMode(
                        AppCompatDelegate
                            .MODE_NIGHT_NO
                    )
            }

            THEME_DARK -> {
                AppCompatDelegate
                    .setDefaultNightMode(
                        AppCompatDelegate
                            .MODE_NIGHT_YES
                    )
            }

            else -> {
                AppCompatDelegate
                    .setDefaultNightMode(
                        AppCompatDelegate
                            .MODE_NIGHT_FOLLOW_SYSTEM
                    )
            }
        }
    }

    fun isSystemTheme(
        context: Context
    ): Boolean {
        return getTheme(context) ==
                THEME_SYSTEM
    }

    fun isLightTheme(
        context: Context
    ): Boolean {
        return getTheme(context) ==
                THEME_LIGHT
    }

    fun isDarkTheme(
        context: Context
    ): Boolean {
        return getTheme(context) ==
                THEME_DARK
    }

    private fun getPrefs(
        context: Context
    ) =
        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
}
