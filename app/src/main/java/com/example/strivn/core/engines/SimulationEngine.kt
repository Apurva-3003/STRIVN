package com.example.strivn.core.engines

import com.example.strivn.core.models.SimulationResult
import com.example.strivn.data.models.UserMetrics

/**
 * Simulates training impact from a hypothetical run without mutating any persisted metrics.
 * Pure Kotlin — no stores or Android APIs.
 */
object SimulationEngine {

    private fun clamp100(value: Int): Int = value.coerceIn(0, 100)

    /**
     * Backend-aligned simulation (matches `backend/app/services/training_model.py`).
     *
     * - load = distanceKm × rpe
     * - fatigueDelta = round(load × 0.14)
     * - fitnessDelta = round(load × 0.035 + durationMinutes × 0.04)
     * - capacity = clamp(fitness - fatigue + sleepScore // 4, 0, 100)
     */
    fun simulateRun(
        currentMetrics: UserMetrics,
        distanceKm: Double,
        durationMinutes: Int,
        rpe: Int,
    ): SimulationResult {
        val beforeFitness = clamp100(currentMetrics.fitness)
        val beforeFatigue = clamp100(currentMetrics.fatigue)
        val beforeCapacity = clamp100(currentMetrics.dailyCapacity)
        val sleepScore = clamp100(currentMetrics.sleep)

        val safeDistance = distanceKm.coerceAtLeast(0.0)
        val safeDuration = durationMinutes.coerceAtLeast(0)
        val safeRpe = rpe.coerceIn(1, 10)

        val load = safeDistance * safeRpe.toDouble()
        val fatigueDelta = kotlin.math.round(load * 0.14).toInt().coerceAtLeast(0)
        val fitnessDelta = kotlin.math.round(load * 0.035 + safeDuration.toDouble() * 0.04).toInt().coerceAtLeast(0)

        val predictedFitness = clamp100(beforeFitness + fitnessDelta)
        val predictedFatigue = clamp100(beforeFatigue + fatigueDelta)
        val predictedCapacity = clamp100(predictedFitness - predictedFatigue + (sleepScore / 4))

        val fitDelta = predictedFitness - beforeFitness
        val fatDelta = predictedFatigue - beforeFatigue
        val capDelta = predictedCapacity - beforeCapacity
        return SimulationResult(
            beforeFitness = beforeFitness,
            beforeFatigue = beforeFatigue,
            beforeCapacity = beforeCapacity,
            predictedFitness = predictedFitness,
            predictedFatigue = predictedFatigue,
            predictedCapacity = predictedCapacity,
            fitnessChange = fitDelta,
            fatigueChange = fatDelta,
            capacityChange = capDelta,
            wheelFitnessChange = fitDelta,
            wheelFatigueChange = fatDelta,
            wheelCapacityChange = capDelta,
        )
    }

    /**
     * Backend-aligned rest day:
     * - fatigue -= round(sleepScore × 0.11)
     * - capacity recalculated with the same capacity formula.
     */
    fun simulateSkipRun(currentMetrics: UserMetrics): SimulationResult {
        val beforeFitness = clamp100(currentMetrics.fitness)
        val beforeFatigue = clamp100(currentMetrics.fatigue)
        val beforeCapacity = clamp100(currentMetrics.dailyCapacity)
        val sleepScore = clamp100(currentMetrics.sleep)

        val fatigueRelief = kotlin.math.round(sleepScore.toDouble() * 0.11).toInt().coerceAtLeast(0)
        val predictedFitness = beforeFitness
        val predictedFatigue = clamp100(beforeFatigue - fatigueRelief)
        val predictedCapacity = clamp100(predictedFitness - predictedFatigue + (sleepScore / 4))

        val fitDelta = predictedFitness - beforeFitness
        val fatDelta = predictedFatigue - beforeFatigue
        val capDelta = predictedCapacity - beforeCapacity
        return SimulationResult(
            beforeFitness = beforeFitness,
            beforeFatigue = beforeFatigue,
            beforeCapacity = beforeCapacity,
            predictedFitness = predictedFitness,
            predictedFatigue = predictedFatigue,
            predictedCapacity = predictedCapacity,
            fitnessChange = fitDelta,
            fatigueChange = fatDelta,
            capacityChange = capDelta,
            wheelFitnessChange = fitDelta,
            wheelFatigueChange = fatDelta,
            wheelCapacityChange = capDelta,
        )
    }
}
