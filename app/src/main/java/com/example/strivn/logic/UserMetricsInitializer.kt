package com.example.strivn.logic

import com.example.strivn.data.models.UserMetrics
import com.example.strivn.data.models.UserProfile
import kotlin.math.roundToInt

/**
 * Initializes UserMetrics from UserProfile after onboarding.
 * Uses deterministic formulas based on profile inputs—no randomness.
 *
 * Logic:
 * - Fitness: scaled from weekly mileage and longest run (higher volume → higher baseline fitness)
 * - Fatigue: low (fresh state), slightly higher if injured
 * - Daily Training Capacity (DTC): fitness − fatigue, mapped to 0–100
 * - Sleep score: derived from avg sleep hours (7–9 optimal)
 * - Injury risk: elevated baseline if injured, else low
 */
object UserMetricsInitializer {

    private const val WEEKLY_MILEAGE_MIN = 10.0
    private const val WEEKLY_MILEAGE_MAX = 100.0
    private const val LONGEST_RUN_MIN = 5.0
    private const val LONGEST_RUN_MAX = 50.0
    private const val SLEEP_MIN = 4.0
    private const val SLEEP_MAX = 12.0

    /**
     * Produces realistic starting UserMetrics from onboarding profile.
     */
    fun fromProfile(profile: UserProfile): UserMetrics {
        val fitness = computeFitness(
            weeklyMileage = profile.weeklyMileage,
            longestRun = profile.longestRun,
            runningDaysPerWeek = profile.runningDaysPerWeek,
        )
        val fatigue = computeFatigue(injuryStatus = profile.injuryStatus)
        val dailyCapacity = computeDailyCapacity(fitness, fatigue)
        val sleep = computeSleepScore(profile.avgSleepHours)
        val injuryRisk = computeInjuryRisk(fatigue, profile.injuryStatus)

        return UserMetrics(
            fitness = fitness,
            fatigue = fatigue,
            sleep = sleep,
            dailyCapacity = dailyCapacity,
            injuryRisk = injuryRisk,
        )
    }

    /**
     * Fitness scaled by weekly mileage (volume), longest run (aerobic base), and running days (consistency).
     * 40 km/wk + 20 km longest + 4 days ≈ 55 fitness; 80 km/wk + 35 km + 6 days ≈ 78.
     */
    private fun computeFitness(
        weeklyMileage: Double,
        longestRun: Double,
        runningDaysPerWeek: Int,
    ): Int {
        val wm = weeklyMileage.coerceIn(WEEKLY_MILEAGE_MIN, WEEKLY_MILEAGE_MAX)
        val lr = longestRun.coerceIn(LONGEST_RUN_MIN, LONGEST_RUN_MAX)
        val days = runningDaysPerWeek.coerceIn(1, 7)
        val mileageContrib = ((wm - WEEKLY_MILEAGE_MIN) / (WEEKLY_MILEAGE_MAX - WEEKLY_MILEAGE_MIN)) * 42 // 0–42
        val longRunContrib = ((lr - LONGEST_RUN_MIN) / (LONGEST_RUN_MAX - LONGEST_RUN_MIN)) * 32 // 0–32
        val daysContrib = ((days - 1) / 6.0) * 12 // 0–12, more days = more consistency
        return (25 + mileageContrib + longRunContrib + daysContrib).roundToInt().coerceIn(25, 90)
    }

    /**
     * Fresh state: low fatigue. Slightly higher if injured (accumulated strain).
     */
    private fun computeFatigue(injuryStatus: Boolean): Int {
        return if (injuryStatus) 38 else 18
    }

    /**
     * DTC maps (fitness − fatigue) from [−100, 100] to [0, 100].
     */
    private fun computeDailyCapacity(fitness: Int, fatigue: Int): Int {
        val delta = (fitness - fatigue).coerceIn(-100, 100)
        return ((delta + 100) / 2).coerceIn(0, 100)
    }

    /**
     * Sleep score from avg hours. Optimal 7–9 h; 6 or 10 still decent; 4 or 12 poor.
     */
    private fun computeSleepScore(avgSleepHours: Double): Int {
        val h = avgSleepHours.coerceIn(SLEEP_MIN, SLEEP_MAX)
        return when {
            h in 7.0..8.5 -> 90  // optimal
            h in 6.5..7.0 -> 82
            h in 8.5..9.5 -> 85
            h in 6.0..6.5 -> 72
            h in 9.5..10.5 -> 75
            h in 5.0..6.0 -> 58
            h in 10.5..12.0 -> 62
            else -> 45  // <5 or edge
        }
    }

    /**
     * Injury risk: base from fatigue, elevated if injured.
     */
    private fun computeInjuryRisk(fatigue: Int, injuryStatus: Boolean): Int {
        val base = (fatigue * 0.4).roundToInt().coerceIn(0, 50)
        return if (injuryStatus) (base + 25).coerceIn(0, 100) else base.coerceIn(0, 100)
    }
}
