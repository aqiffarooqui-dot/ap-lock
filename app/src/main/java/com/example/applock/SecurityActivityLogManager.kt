package com.example.applock

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SecurityLogEntry(
    val id: Long,
    val timestamp: Long,
    val type: String,
    val title: String,
    val details: String
)

object SecurityActivityLogManager {

    private const val PREF_NAME =
        "FarooquiSecurityActivity"

    private const val KEY_LOGS =
        "security_logs"

    private const val MAX_LOGS =
        300

    const val TYPE_UNLOCK =
        "unlock"

    const val TYPE_FAILED_UNLOCK =
        "failed_unlock"

    const val TYPE_APP_LOCKED =
        "app_locked"

    const val TYPE_APP_UNLOCKED =
        "app_unlocked"

    const val TYPE_INTRUSION =
        "intrusion"

    const val TYPE_SETTING_CHANGED =
        "setting_changed"

    const val TYPE_SECURITY =
        "security"

    fun addLog(
        context: Context,
        type: String,
        title: String,
        details: String = ""
    ) {

        val logs =
            getLogs(context).toMutableList()

        logs.add(
            0,
            SecurityLogEntry(
                id = System.currentTimeMillis(),
                timestamp = System.currentTimeMillis(),
                type = type,
                title = title,
                details = details
            )
        )

        saveLogs(
            context,
            logs.take(MAX_LOGS)
        )
    }

    fun getLogs(
        context: Context
    ): List<SecurityLogEntry> {

        val json =
            getPrefs(context)
                .getString(
                    KEY_LOGS,
                    null
                )
                ?: return emptyList()

        return try {

            val array =
                JSONArray(json)

            val logs =
                mutableListOf<SecurityLogEntry>()

            for (
                index in 0 until array.length()
            ) {

                val item =
                    array.getJSONObject(index)

                logs.add(
                    SecurityLogEntry(
                        id =
                            item.optLong("id"),

                        timestamp =
                            item.optLong("timestamp"),

                        type =
                            item.optString("type"),

                        title =
                            item.optString("title"),

                        details =
                            item.optString("details")
                    )
                )
            }

            logs

        } catch (_: Exception) {

            emptyList()
        }
    }

    fun getRecentLogs(
        context: Context,
        limit: Int = 20
    ): List<SecurityLogEntry> {

        return getLogs(context)
            .take(
                limit.coerceAtLeast(1)
            )
    }

    fun getLogsByType(
        context: Context,
        type: String
    ): List<SecurityLogEntry> {

        return getLogs(context)
            .filter {
                it.type == type
            }
    }

    fun getTodayLogs(
        context: Context
    ): List<SecurityLogEntry> {

        val calendar =
            java.util.Calendar
                .getInstance()

        calendar.set(
            java.util.Calendar.HOUR_OF_DAY,
            0
        )

        calendar.set(
            java.util.Calendar.MINUTE,
            0
        )

        calendar.set(
            java.util.Calendar.SECOND,
            0
        )

        calendar.set(
            java.util.Calendar.MILLISECOND,
            0
        )

        val startOfDay =
            calendar.timeInMillis

        return getLogs(context)
            .filter {
                it.timestamp >= startOfDay
            }
    }

    fun deleteLog(
        context: Context,
        id: Long
    ) {

        val updated =
            getLogs(context)
                .filterNot {
                    it.id == id
                }

        saveLogs(
            context,
            updated
        )
    }

    fun clearAll(
        context: Context
    ) {

        getPrefs(context)
            .edit()
            .remove(
                KEY_LOGS
            )
            .apply()
    }

    private fun saveLogs(
        context: Context,
        logs: List<SecurityLogEntry>
    ) {

        val array =
            JSONArray()

        logs.forEach { log ->

            array.put(
                JSONObject().apply {

                    put(
                        "id",
                        log.id
                    )

                    put(
                        "timestamp",
                        log.timestamp
                    )

                    put(
                        "type",
                        log.type
                    )

                    put(
                        "title",
                        log.title
                    )

                    put(
                        "details",
                        log.details
                    )
                }
            )
        }

        getPrefs(context)
            .edit()
            .putString(
                KEY_LOGS,
                array.toString()
            )
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
