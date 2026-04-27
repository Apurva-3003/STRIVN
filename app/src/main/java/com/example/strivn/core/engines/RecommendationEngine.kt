package com.example.strivn.core.engines

import com.example.strivn.data.models.Observation
import com.example.strivn.data.models.RunRecommendation
import com.example.strivn.data.models.UserMetrics
import com.example.strivn.data.models.UserProfile
import com.example.strivn.data.repository.RunHistoryStore
import kotlin.math.roundToInt

/** Alias for [RunRecommendation] (same shape as the app model). */
typealias Recommendation = RunRecommendation

/**
 * Pure Kotlin recommendation engine: workout type, distance from profile volume, duration, RPE, injury risk.
 * Distance and intensity scale down when [UserMetrics.fitness] is low or [UserMetrics.fatigue] is high.
 */
object RecommendationEngine {

    /**
     * @param profile If null, uses conservative defaults (weekly mileage / run days).
     */
    fun generateRecommendation(
        profile: UserProfile?,
        metrics: UserMetrics,
        observation: Observation,
    ): Recommendation {
        val weeklyMileage = profile?.weeklyMileage?.coerceAtLeast(1.0) ?: 40.0
        val runningDaysPerWeek = (profile?.runningDaysPerWeek ?: 5).coerceIn(1, 7)

        val baseDistance = weeklyMileage / runningDaysPerWeek.toDouble()

        val workout = applyHardDayGuard(
            selected = selectWorkout(metrics, observation),
            lastRunRpe = RunHistoryStore.getLastRun()?.rpe,
        )
        val readinessMultiplier = workout.multiplier

        var distanceKm = (baseDistance * readinessMultiplier).coerceAtLeast(0.0)
        if (metrics.fitness < 30) {
            distanceKm *= 0.42
        }
        if (metrics.fatigue > 70) {
            distanceKm *= 0.55
        }
        distanceKm = distanceKm.coerceAtLeast(0.0)

        val durationMinutes = (distanceKm * 8.0).roundToInt().coerceAtLeast(0)

        val sleepScore = metrics.sleep.coerceIn(0, 100)
        val injuryRisk = (metrics.fatigue - sleepScore / 2).coerceIn(0, 100)

        return RunRecommendation(
            type = workout.typeLabel,
            focus = workout.focus,
            environment = "Outdoor",
            rpe = workout.rpe,
            distanceKm = distanceKm,
            durationMin = durationMinutes,
            injuryRisk = injuryRisk,
        )
    }

    private enum class Workout(
        val typeLabel: String,
        val focus: String,
        val multiplier: Double,
        val rpe: Int,
    ) {
        REST_OR_RECOVERY(
            typeLabel = "Rest or Recovery Run",
            focus = "Recovery",
            multiplier = 0.55,
            rpe = 3,
        ),
        EASY(
            typeLabel = "Easy Aerobic Run",
            focus = "Aerobic Base",
            multiplier = 0.95,
            rpe = 4,
        ),
        STEADY(
            typeLabel = "Steady Run",
            focus = "Steady Aerobic",
            multiplier = 1.15,
            rpe = 6,
        ),
        TEMPO(
            typeLabel = "Tempo Run",
            focus = "Threshold",
            multiplier = 1.35,
            rpe = 7,
        ),
        INTERVALS(
            typeLabel = "Intervals",
            focus = "VO2 / Speed",
            multiplier = 1.35,
            rpe = 8,
        ),
    }

    /**
     * If the most recent run in [RunHistoryStore] was hard (RPE ≥ 7), do not prescribe Tempo or Intervals
     * the next day — downgrade to easy aerobic to reduce back-to-back quality stress.
     */
    private fun applyHardDayGuard(selected: Workout, lastRunRpe: Int?): Workout {
        if (lastRunRpe == null || lastRunRpe < 7) return selected
        return when (selected) {
            Workout.TEMPO, Workout.INTERVALS -> Workout.EASY
            else -> selected
        }
    }

    /**
     * Low fitness → easy only. Fatigue > 70 → rest/recovery or easy only (no steady/tempo/intervals).
     * Otherwise use capacity bands.
     */
    private fun selectWorkout(metrics: UserMetrics, observation: Observation): Workout {
        val capacity = metrics.dailyCapacity
        val fatigue = metrics.fatigue
        val fitness = metrics.fitness

        if (fitness < 30) return Workout.EASY

        if (fatigue > 70) {
            return if (capacity < 42 || fatigue > 85) {
                Workout.REST_OR_RECOVERY
            } else {
                Workout.EASY
            }
        }

        if (capacity < 40) return Workout.REST_OR_RECOVERY

        if (capacity in 60..80) return Workout.STEADY
        if (capacity in 40..59) return Workout.EASY

        val primed = observation.state == "Primed for Progress"
        return if (primed) Workout.INTERVALS else Workout.TEMPO
    }
}
