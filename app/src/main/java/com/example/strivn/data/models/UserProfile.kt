package com.example.strivn.data.models

/**
 * User profile from onboarding.
 * Used by the training algorithm for personalized recommendations.
 *
 * @param raceDate yyyy-MM-dd format (compatible with minSdk 24)
 */
data class UserProfile(
    val goal: String,
    val raceDate: String,
    val weeklyMileage: Double,
    val longestRun: Double,
    val runningDaysPerWeek: Int,
    val injuryStatus: Boolean,
    val avgSleepHours: Double,
)
