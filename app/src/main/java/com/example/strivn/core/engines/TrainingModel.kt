package com.example.strivn.core.engines

import com.example.strivn.data.models.DailyCheckIn
import com.example.strivn.data.models.UserMetrics
import kotlin.math.roundToInt

/**
 * Pure Kotlin training-state engine: updates [UserMetrics] from check-ins and runs.
 * No Android dependencies.
 *
 * [UserMetrics] stores fitness/fatigue on a 0..100 scale; internally this model uses
 * a 0..200 scale (stored × 2) so capacity math matches the spec.
 */
object TrainingModel {

    /**
     * Morning check-in: adjusts fatigue from soreness/sleep quality, recalculates sleep score,
     * then derives capacity from fitness, fatigue, sleep, energy, and stress.
     */
    fun updateFromCheckIn(
        currentMetrics: UserMetrics,
        checkIn: DailyCheckIn,
    ): UserMetrics {
        var fitness = fromStoredFitness200(currentMetrics.fitness)
        var fatigue = fromStoredFatigue200(currentMetrics.fatigue)

        val soreness = checkIn.muscleSoreness.coerceIn(1, 5)
        val sleepQuality = checkIn.sleepQuality.coerceIn(1, 5)

        // Muscle soreness adds load; sleep quality reduces it (was too weak vs soreness).
        fatigue += soreness * 1.25
        fatigue -= sleepQuality * 1.75
        // Hours slept: more recovery when sleep duration is adequate.
        val hours = checkIn.sleepHours.coerceIn(0.0, 12.0)
        fatigue -= (hours / 12.0) * 4.0
        fatigue = fatigue.coerceIn(0.0, 200.0)

        val sleepScore = (checkIn.sleepHours * 10.0)
            .coerceIn(30.0, 100.0)
            .roundToInt()
            .coerceIn(0, 100)

        val energy = checkIn.energyLevel.coerceIn(1, 5)
        val stress = checkIn.stressLevel.coerceIn(1, 5)

        val capacity = computeCheckInCapacity(
            fitness = fitness,
            fatigue = fatigue,
            sleepScore = sleepScore,
            energy = energy,
            stress = stress,
        )

        return UserMetrics(
            fatigue = toStoredFatigue(fatigue),
            fitness = toStoredFitness(fitness),
            sleep = sleepScore,
            dailyCapacity = capacity,
            injuryRisk = currentMetrics.injuryRisk,
        )
    }

    /**
     * Internal snapshot after applying run load (0–200 fitness/fatigue + stored 0–100).
     * Used by [updateFromRun] and [SimulationEngine] so capacity uses the same inputs.
     */
    internal data class RunLoadOutcome(
        val fitness200: Int,
        val fatigue200: Int,
        val fitnessStored: Int,
        val fatigueStored: Int,
    )

    /**
     * Same as [updateFromRun], plus the [RunLoadOutcome] used for capacity (0–200 fitness/fatigue).
     * Single pass over run-load math — used by [SimulationEngine] to call [computeTrainingCapacity] explicitly.
     */
    internal fun runSimulationAfterLoad(
        currentMetrics: UserMetrics,
        distanceKm: Double,
        durationMinutes: Int,
        rpe: Int,
    ): Pair<UserMetrics, RunLoadOutcome> {
        val outcome = runLoadOutcome(
            currentMetrics = currentMetrics,
            distanceKm = distanceKm,
            durationMinutes = durationMinutes,
            rpe = rpe,
        )
        val sleepScore = currentMetrics.sleep.coerceIn(0, 100)
        val capacity = computeTrainingCapacity(
            fitness = outcome.fitness200,
            fatigue = outcome.fatigue200,
            sleepScore = sleepScore,
        )
        val metrics = UserMetrics(
            fatigue = outcome.fatigueStored,
            fitness = outcome.fitnessStored,
            sleep = sleepScore,
            dailyCapacity = capacity,
            injuryRisk = currentMetrics.injuryRisk,
        )
        return metrics to outcome
    }

    /**
     * After a logged run: training load from distance × RPE, then fatigue/fitness adaptation
     * and capacity from [computeTrainingCapacity].
     *
     * @param durationMinutes reserved for future load models (not used in current formulas).
     */
    fun updateFromRun(
        currentMetrics: UserMetrics,
        distanceKm: Double,
        durationMinutes: Int,
        rpe: Int,
    ): UserMetrics = runSimulationAfterLoad(
        currentMetrics = currentMetrics,
        distanceKm = distanceKm,
        durationMinutes = durationMinutes,
        rpe = rpe,
    ).first

    /**
     * Rest day with no training load: fatigue decreases slightly (recovery); fitness unchanged.
     * Used by [SimulationEngine.simulateSkipRun] — does not persist.
     */
    fun predictPassiveRecoveryDay(currentMetrics: UserMetrics): UserMetrics {
        var fitness = fromStoredFitness200(currentMetrics.fitness)
        var fatigue = fromStoredFatigue200(currentMetrics.fatigue)
        fatigue -= 4.0
        fatigue = fatigue.coerceIn(0.0, 200.0)
        val sleepScore = currentMetrics.sleep.coerceIn(0, 100)
        val fitness200 = fitness.roundToInt().coerceIn(0, 200)
        val fatigue200 = fatigue.roundToInt().coerceIn(0, 200)
        val capacity = computeTrainingCapacity(
            fitness = fitness200,
            fatigue = fatigue200,
            sleepScore = sleepScore,
        )
        return UserMetrics(
            fatigue = toStoredFatigue(fatigue),
            fitness = toStoredFitness(fitness),
            sleep = sleepScore,
            dailyCapacity = capacity,
            injuryRisk = currentMetrics.injuryRisk,
        )
    }

    /**
     * Applies the same run-load math as [updateFromRun] without persisting.
     */
    internal fun runLoadOutcome(
        currentMetrics: UserMetrics,
        distanceKm: Double,
        durationMinutes: Int,
        rpe: Int,
    ): RunLoadOutcome {
        var fitness = fromStoredFitness200(currentMetrics.fitness)
        var fatigue = fromStoredFatigue200(currentMetrics.fatigue)

        val trainingLoad = distanceKm * rpe.coerceIn(1, 10)

        fatigue += trainingLoad * 0.7
        fitness += trainingLoad * 0.2

        fitness = fitness.coerceIn(0.0, 200.0)
        fatigue = fatigue.coerceIn(0.0, 200.0)

        val fitness200 = fitness.roundToInt().coerceIn(0, 200)
        val fatigue200 = fatigue.roundToInt().coerceIn(0, 200)

        return RunLoadOutcome(
            fitness200 = fitness200,
            fatigue200 = fatigue200,
            fitnessStored = toStoredFitness(fitness),
            fatigueStored = toStoredFatigue(fatigue),
        )
    }

    /**
     * @param fitness 0..200
     * @param fatigue 0..200
     * @param sleepScore 0..100
     */
    fun computeTrainingCapacity(
        fitness: Int,
        fatigue: Int,
        sleepScore: Int,
    ): Int {
        val f = fitness.coerceIn(0, 200)
        val t = fatigue.coerceIn(0, 200)
        val s = sleepScore.coerceIn(0, 100)
        // Raw sum often exceeds 100; halve to map 0–200 scale into usable DTC (matches MetricsInitializer).
        val sum = f - t + s / 2
        return (sum / 2).coerceIn(0, 100)
    }

    private fun computeCheckInCapacity(
        fitness: Double,
        fatigue: Double,
        sleepScore: Int,
        energy: Int,
        stress: Int,
    ): Int {
        val f = fitness.roundToInt().coerceIn(0, 200)
        val t = fatigue.roundToInt().coerceIn(0, 200)
        val s = sleepScore.coerceIn(0, 100)
        val base = (f - t + s / 2.0) / 2.0
        val adjusted = base + energy * 2.0 - stress * 1.5
        return adjusted.roundToInt().coerceIn(0, 100)
    }

    /** Stored 0..100 → internal 0..200 */
    private fun fromStoredFitness200(stored: Int): Double =
        stored.coerceIn(0, 100) * 2.0

    /** Stored 0..100 → internal 0..200 */
    private fun fromStoredFatigue200(stored: Int): Double =
        stored.coerceIn(0, 100) * 2.0

    /** Internal 0..200 → stored 0..100 */
    private fun toStoredFitness(fitness: Double): Int =
        (fitness / 2.0).roundToInt().coerceIn(0, 100)

    /** Internal 0..200 → stored 0..100 */
    private fun toStoredFatigue(fatigue: Double): Int =
        (fatigue / 2.0).roundToInt().coerceIn(0, 100)
}
