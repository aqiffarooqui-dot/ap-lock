package com.example.applock

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class IntrusionRecord(
    val id: Long,
    val timestamp: Long,
    val packageName: String,
    val appName: String,
    val imageUri: String?
)

object IntrusionManager {

    private const val PREF_NAME =
        "FarooquiIntrusionHistory"

    private const val KEY_RECORDS =
        "intrusion_records"

    private const val MAX_RECORDS =
        200

    fun addIntrusion(
        context: Context,
        packageName: String,
        appName: String,
        imageUri: String? = null
    ) {

        val records =
            getRecords(context).toMutableList()

        records.add(
            0,
            IntrusionRecord(
                id = System.currentTimeMillis(),
                timestamp = System.currentTimeMillis(),
                packageName = packageName,
                appName = appName,
                imageUri = imageUri
            )
        )

        val limitedRecords =
            records.take(MAX_RECORDS)

        saveRecords(
            context,
            limitedRecords
        )
    }

    fun getRecords(
        context: Context
    ): List<IntrusionRecord> {

        val json =
            getPrefs(context)
                .getString(
                    KEY_RECORDS,
                    null
                )
                ?: return emptyList()

        return try {

            val array =
                JSONArray(json)

            val records =
                mutableListOf<IntrusionRecord>()

            for (index in 0 until array.length()) {

                val item =
                    array.getJSONObject(index)

                records.add(
                    IntrusionRecord(
                        id = item.optLong(
                            "id"
                        ),
                        timestamp = item.optLong(
                            "timestamp"
                        ),
                        packageName = item.optString(
                            "packageName"
                        ),
                        appName = item.optString(
                            "appName"
                        ),
                        imageUri =
                            if (
                                item.isNull(
                                    "imageUri"
                                )
                            ) {
                                null
                            } else {
                                item.optString(
                                    "imageUri"
                                )
                            }
                    )
                )
            }

            records

        } catch (_: Exception) {

            emptyList()
        }
    }

    fun getTotalAttempts(
        context: Context
    ): Int {

        return getRecords(context).size
    }

    fun getTodayAttempts(
        context: Context
    ): Int {

        val startOfDay =
            java.util.Calendar
                .getInstance()
                .apply {

                    set(
                        java.util.Calendar.HOUR_OF_DAY,
                        0
                    )

                    set(
                        java.util.Calendar.MINUTE,
                        0
                    )

                    set(
                        java.util.Calendar.SECOND,
                        0
                    )

                    set(
                        java.util.Calendar.MILLISECOND,
                        0
                    )

                }
                .timeInMillis

        return getRecords(context)
            .count {
                it.timestamp >= startOfDay
            }
    }

    fun getLastAttempt(
        context: Context
    ): IntrusionRecord? {

        return getRecords(context)
            .firstOrNull()
    }

    fun deleteRecord(
        context: Context,
        id: Long
    ) {

        val updated =
            getRecords(context)
                .filterNot {
                    it.id == id
                }

        saveRecords(
            context,
            updated
        )
    }

    fun deleteAll(
        context: Context
    ) {

        getPrefs(context)
            .edit()
            .remove(
                KEY_RECORDS
            )
            .apply()
    }

    fun getAttemptsForApp(
        context: Context,
        packageName: String
    ): List<IntrusionRecord> {

        return getRecords(context)
            .filter {
                it.packageName == packageName
            }
    }

    private fun saveRecords(
        context: Context,
        records: List<IntrusionRecord>
    ) {

        val array =
            JSONArray()

        records.forEach { record ->

            val item =
                JSONObject().apply {

                    put(
                        "id",
                        record.id
                    )

                    put(
                        "timestamp",
                        record.timestamp
                    )

                    put(
                        "packageName",
                        record.packageName
                    )

                    put(
                        "appName",
                        record.appName
                    )

                    put(
                        "imageUri",
                        record.imageUri
                    )
                }

            array.put(item)
        }

        getPrefs(context)
            .edit()
            .putString(
                KEY_RECORDS,
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
