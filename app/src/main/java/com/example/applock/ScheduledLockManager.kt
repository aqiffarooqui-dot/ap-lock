package com.example.applock

import android.content.Context
import java.util.Calendar

object ScheduledLockManager {

    private const val PREF_NAME =
        "FarooquiScheduledLocks"

    private const val KEY_ENABLED =
        "schedule_enabled"

    private const val KEY_START_HOUR =
        "start_hour"

    private const val KEY_START_MINUTE =
        "start_minute"

    private const val KEY_END_HOUR =
        "end_hour"

    private const val KEY_END_MINUTE =
        "end_minute"

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

    fun setSchedule(
        context: Context,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) {
        getPrefs(context)
            .edit()
            .putInt(
                KEY_START_HOUR,
                startHour
            )
            .putInt(
                KEY_START_MINUTE,
                startMinute
            )
            .putInt(
                KEY_END_HOUR,
                endHour
            )
            .putInt(
                KEY_END_MINUTE,
                endMinute
            )
            .apply()
    }

    fun getStartHour(
        context: Context
    ): Int {
        return getPrefs(context)
            .getInt(
                KEY_START_HOUR,
                22
            )
    }

    fun getStartMinute(
        context: Context
    ): Int {
        return getPrefs(context)
            .getInt(
                KEY_START_MINUTE,
                0
            )
    }

    fun getEndHour(
        context: Context
    ): Int {
        return getPrefs(context)
            .getInt(
                KEY_END_HOUR,
                7
            )
    }

    fun getEndMinute(
        context: Context
    ): Int {
        return getPrefs(context)
            .getInt(
                KEY_END_MINUTE,
                0
            )
    }

    fun isInsideSchedule(
        context: Context
    ): Boolean {

        if (!isEnabled(context)) {
            return false
        }

        val now =
            Calendar.getInstance()

        val currentMinutes =
            now.get(
                Calendar.HOUR_OF_DAY
            ) * 60 +
                    now.get(
                        Calendar.MINUTE
                    )

        val startMinutes =
            getStartHour(context) * 60 +
                    getStartMinute(context)

        val endMinutes =
            getEndHour(context) * 60 +
                    getEndMinute(context)

        /*
         * Same start/end means the schedule is
         * considered active for the full day.
         */
        if (startMinutes == endMinutes) {
            return true
        }

        /*
         * Normal schedule:
         * 09:00 -> 18:00
         */
        if (startMinutes < endMinutes) {
            return currentMinutes >= startMinutes &&
                    currentMinutes < endMinutes
        }

        /*
         * Overnight schedule:
         * 22:00 -> 07:00
         */
        return currentMinutes >= startMinutes ||
                currentMinutes < endMinutes
    }

    fun getScheduleText(
        context: Context
    ): String {

        val start =
            formatTime(
                getStartHour(context),
                getStartMinute(context)
            )

        val end =
            formatTime(
                getEndHour(context),
                getEndMinute(context)
            )

        return "$start – $end"
    }

    private fun formatTime(
        hour: Int,
        minute: Int
    ): String {

        val suffix =
            if (hour >= 12) "PM" else "AM"

        val displayHour =
            when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }

        return String.format(
            "%02d:%02d %s",
            displayHour,
            minute,
            suffix
        )
    }

    private fun getPrefs(
        context: Context
    ) =
        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
}
