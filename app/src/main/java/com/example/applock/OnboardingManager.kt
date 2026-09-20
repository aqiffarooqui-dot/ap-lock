package com.example.applock

import android.content.Context

object OnboardingManager {

    private const val PREF_NAME =
        "FarooquiOnboarding"

    private const val KEY_COMPLETED =
        "onboarding_completed"

    private const val KEY_CURRENT_STEP =
        "current_step"

    private const val TOTAL_STEPS =
        5

    fun isCompleted(
        context: Context
    ): Boolean {

        return getPrefs(context)
            .getBoolean(
                KEY_COMPLETED,
                false
            )
    }

    fun setCompleted(
        context: Context,
        completed: Boolean
    ) {

        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_COMPLETED,
                completed
            )
            .apply()
    }

    fun getCurrentStep(
        context: Context
    ): Int {

        return getPrefs(context)
            .getInt(
                KEY_CURRENT_STEP,
                0
            )
            .coerceIn(
                0,
                TOTAL_STEPS - 1
            )
    }

    fun setCurrentStep(
        context: Context,
        step: Int
    ) {

        getPrefs(context)
            .edit()
            .putInt(
                KEY_CURRENT_STEP,
                step.coerceIn(
                    0,
                    TOTAL_STEPS - 1
                )
            )
            .apply()
    }

    fun nextStep(
        context: Context
    ): Int {

        val next =
            (
                getCurrentStep(context) + 1
            ).coerceAtMost(
                TOTAL_STEPS - 1
            )

        setCurrentStep(
            context,
            next
        )

        return next
    }

    fun previousStep(
        context: Context
    ): Int {

        val previous =
            (
                getCurrentStep(context) - 1
            ).coerceAtLeast(0)

        setCurrentStep(
            context,
            previous
        )

        return previous
    }

    fun isFirstStep(
        context: Context
    ): Boolean {

        return getCurrentStep(context) == 0
    }

    fun isLastStep(
        context: Context
    ): Boolean {

        return getCurrentStep(context) ==
                TOTAL_STEPS - 1
    }

    fun getTotalSteps(): Int {
        return TOTAL_STEPS
    }

    fun getProgress(
        context: Context
    ): Float {

        return (
            (getCurrentStep(context) + 1)
                .toFloat() /
                    TOTAL_STEPS.toFloat()
            )
    }

    fun complete(
        context: Context
    ) {

        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_COMPLETED,
                true
            )
            .putInt(
                KEY_CURRENT_STEP,
                TOTAL_STEPS - 1
            )
            .apply()
    }

    fun reset(
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
