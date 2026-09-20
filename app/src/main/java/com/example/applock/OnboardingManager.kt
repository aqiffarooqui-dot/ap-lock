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

        return getPrefs(context
