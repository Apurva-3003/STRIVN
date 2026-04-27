package com.example.strivn.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.strivn.data.models.UserProfile

/**
 * Persists onboarding completion flag and UserProfile using SharedPreferences.
 * Used to skip onboarding on subsequent launches.
 */
class OnboardingPreferences(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var isOnboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
        set(value) {
            // commit() so reads after onboarding/auth complete are not stale (apply() is async).
            prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, value).commit()
        }

    fun getProfile(): UserProfile? {
        val goal = prefs.getString(KEY_GOAL, null) ?: return null
        val raceDate = prefs.getString(KEY_RACE_DATE, null) ?: return null
        return UserProfile(
            goal = goal,
            raceDate = raceDate,
            weeklyMileage = prefs.getFloat(KEY_WEEKLY_MILEAGE, -1f).toDouble().takeIf { it >= 0 } ?: return null,
            longestRun = prefs.getFloat(KEY_LONGEST_RUN, -1f).toDouble().takeIf { it >= 0 } ?: return null,
            runningDaysPerWeek = prefs.getInt(KEY_RUNNING_DAYS, -1).takeIf { it in 1..7 } ?: return null,
            injuryStatus = prefs.getBoolean(KEY_INJURY_STATUS, false),
            avgSleepHours = prefs.getFloat(KEY_AVG_SLEEP, -1f).toDouble().takeIf { it >= 0 } ?: return null,
        )
    }

    fun saveProfile(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_GOAL, profile.goal)
            .putString(KEY_RACE_DATE, profile.raceDate)
            .putFloat(KEY_WEEKLY_MILEAGE, profile.weeklyMileage.toFloat())
            .putFloat(KEY_LONGEST_RUN, profile.longestRun.toFloat())
            .putInt(KEY_RUNNING_DAYS, profile.runningDaysPerWeek)
            .putBoolean(KEY_INJURY_STATUS, profile.injuryStatus)
            .putFloat(KEY_AVG_SLEEP, profile.avgSleepHours.toFloat())
            .apply()
    }

    /**
     * Persists profile and completion in one synchronous write so the next UI frame
     * sees [isOnboardingComplete] == true (avoids bouncing back into onboarding after register).
     */
    fun completeOnboarding(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_GOAL, profile.goal)
            .putString(KEY_RACE_DATE, profile.raceDate)
            .putFloat(KEY_WEEKLY_MILEAGE, profile.weeklyMileage.toFloat())
            .putFloat(KEY_LONGEST_RUN, profile.longestRun.toFloat())
            .putInt(KEY_RUNNING_DAYS, profile.runningDaysPerWeek)
            .putBoolean(KEY_INJURY_STATUS, profile.injuryStatus)
            .putFloat(KEY_AVG_SLEEP, profile.avgSleepHours.toFloat())
            .putBoolean(KEY_ONBOARDING_COMPLETE, true)
            .commit()
    }

    companion object {
        private const val PREFS_NAME = "strivn_onboarding"
        private const val KEY_ONBOARDING_COMPLETE = "is_onboarding_complete"
        private const val KEY_GOAL = "profile_goal"
        private const val KEY_RACE_DATE = "profile_race_date"
        private const val KEY_WEEKLY_MILEAGE = "profile_weekly_mileage"
        private const val KEY_LONGEST_RUN = "profile_longest_run"
        private const val KEY_RUNNING_DAYS = "profile_running_days"
        private const val KEY_INJURY_STATUS = "profile_injury_status"
        private const val KEY_AVG_SLEEP = "profile_avg_sleep"
    }
}
