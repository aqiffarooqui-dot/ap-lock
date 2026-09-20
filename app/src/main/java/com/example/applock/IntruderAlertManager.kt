package com.example.applock

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build

object IntruderAlertManager {

    private const val PREF_NAME =
        "FarooquiIntruderAlerts"

    private const val KEY_ENABLED =
        "alert_enabled"

    private const val KEY_SOUND =
        "alert_sound"

    private const val KEY_VIBRATION =
        "alert_vibration"

    private const val KEY_FAILED_ATTEMPTS =
        "failed_attempt_threshold"

    fun isEnabled(
        context: Context
    ): Boolean {

        return getPrefs(context)
            .getBoolean(
                KEY_ENABLED,
                true
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

    fun isSoundEnabled(
        context: Context
    ): Boolean {

        return getPrefs(context)
            .getBoolean(
                KEY_SOUND,
                true
            )
    }

    fun setSoundEnabled(
        context: Context,
        enabled: Boolean
    ) {

        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_SOUND,
                enabled
            )
            .apply()
    }

    fun isVibrationEnabled(
        context: Context
    ): Boolean {

        return getPrefs(context)
            .getBoolean(
                KEY_VIBRATION,
                true
            )
    }

    fun setVibrationEnabled(
        context: Context,
        enabled: Boolean
    ) {

        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_VIBRATION,
                enabled
            )
            .apply()
    }

    fun getFailedAttemptThreshold(
        context: Context
    ): Int {

        return getPrefs(context)
            .getInt(
                KEY_FAILED_ATTEMPTS,
                1
            )
    }

    fun setFailedAttemptThreshold(
        context: Context,
        attempts: Int
    ) {

        val safeValue =
            attempts.coerceIn(
                1,
                10
            )

        getPrefs(context)
            .edit()
            .putInt(
                KEY_FAILED_ATTEMPTS,
                safeValue
            )
            .apply()
    }

    fun triggerAlert(
        context: Context
    ) {

        if (!isEnabled(context)) {
            return
        }

        if (isSoundEnabled(context)) {
            playWarningSound()
        }

        if (isVibrationEnabled(context)) {
            vibrate(context)
        }
    }

    private fun playWarningSound() {

        var toneGenerator: ToneGenerator? = null

        try {

            toneGenerator =
                ToneGenerator(
                    AudioManager.STREAM_ALARM,
                    80
                )

            toneGenerator.startTone(
                ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
                500
            )

        } catch (_: Exception) {

        } finally {

            try {
                toneGenerator?.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun vibrate(
        context: Context
    ) {

        try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S
            ) {

                val vibratorManager =
                    context.getSystemService(
                        VibratorManager::class.java
                    )

                vibratorManager
                    ?.defaultVibrator
                    ?.vibrate(
                        VibrationEffect.createOneShot(
                            350L,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )

            } else {

                @Suppress("DEPRECATION")
                val vibrator =
                    context.getSystemService(
                        Context.VIBRATOR_SERVICE
                    ) as? Vibrator

                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.O
                ) {

                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(
                            350L,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )

                } else {

                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(350L)
                }
            }

        } catch (_: Exception) {

        }
    }

    private fun getPrefs(
        context: Context
    ) =
        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
}
