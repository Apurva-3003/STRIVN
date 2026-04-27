package com.example.strivn.core.engines

import com.example.strivn.data.models.UserMetrics
import com.example.strivn.data.models.UserProfile
import kotlin.math.roundToInt

/**
 * Pure Kotlin engine: derives starting [UserMetrics] from onboarding [UserProfile].
 * No Android or UI dependencies — safe for ViewModels, tests, or future backend use.
 *
 * Fitness is computed on a 0..200 scale then mapped to 0..100 for storage ([UserMetrics] convention).
 * DTC matches [TrainingModel.computeTrainingCapacity] (fatigue on 0–200 scale: stored × 2, then sum / 2).
 */
object MetricsInitializer {

    fun initializeMetrics(profile: UserProfile): UserMetrics {
        val fitness200 = computeFitness200(profile)
        val fatigueStored = computeFatigue(profile)
        val sleepScore = computeSleepScore(profile.avgSleepHours)

        // Use same 0–200 fatigue scale as TrainingModel (stored fatigue × 2), then same DTC mapping as computeTrainingCapacity.
        val fatigue200 = fatigueStored.coerceIn(0, 100) * 2
        val sum = fitness200 - fatigue200 + sleepScore / 2
        val capacity = (sum / 2).coerceIn(0, 100)

        // Map 0..200 fitness scale into 0..100 for UserMetrics
        val fitnessStored = (fitness200 / 2).coerceIn(0, 100)

        return UserMetrics(
            fatigue = fatigueStored.coerceIn(0, 100),
            fitness = fitnessStored,
            sleep = sleepScore,
            dailyCapacity = capacity,
            injuryRisk = if (profile.injuryStatus) 25 else 0,
        )
    }

    private fun computeFitness200(profile: UserProfile): Int {
        val raw = profile.weeklyMileage * 2.0 + profile.longestRun * 3.0
        return raw.roundToInt().coerceIn(0, 200)
    }

    private fun computeFatigue(profile: UserProfile): Int =
        if (profile.injuryStatus) 30 else 10

    private fun computeSleepScore(avgSleepHours: Double): Int = when {
        avgSleepHours >= 8.0 -> 90
        avgSleepHours >= 7.0 -> 75
        avgSleepHours >= 6.0 -> 60
        else -> 40
    }
}
