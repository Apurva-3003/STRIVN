package com.example.strivn.data.repository

import com.example.strivn.core.engines.TrainingModel
import com.example.strivn.data.models.MetricsSnapshot
import com.example.strivn.data.models.RunLog
import com.example.strivn.data.models.UserMetrics

/**
 * Shared path for logging a run: apply [TrainingModel.updateFromRun], build [RunLog] (including metric deltas),
 * persist via [RunHistoryStore.addRun], update [InMemoryUserMetricsStore], record [MetricsSnapshot] in
 * [MetricsHistoryStore]. Used by [com.example.strivn.ui.screens.home.HomeViewModel.logRun]
 * and [com.example.strivn.ui.screens.simulation.SimulationViewModel] so persistence stays identical.
 */
object RunLogPersistence {

    /**
     * Reads [InMemoryUserMetricsStore.metrics], applies [TrainingModel.updateFromRun], builds [RunLog]
     * (including before/after fitness, fatigue, and daily training capacity from [UserMetrics.dailyCapacity]),
     * then persists the run and updates stores.
     *
     * @return Metrics after the run is applied (also written to the store).
     */
    fun logRunAndUpdateMetrics(
        distanceKm: Double,
        durationMinutes: Int,
        rpe: Int,
    ): UserMetrics {
        val oldMetrics = InMemoryUserMetricsStore.metrics

        val updatedMetrics = TrainingModel.updateFromRun(
            currentMetrics = oldMetrics,
            distanceKm = distanceKm,
            durationMinutes = durationMinutes,
            rpe = rpe,
        )

        val fitnessChange = updatedMetrics.fitness - oldMetrics.fitness
        val fatigueChange = updatedMetrics.fatigue - oldMetrics.fatigue
        val capacityChange = updatedMetrics.dailyCapacity - oldMetrics.dailyCapacity

        val runLog = RunLog(
            date = todayLocalDate(),
            distanceKm = distanceKm,
            durationMinutes = durationMinutes,
            rpe = rpe,
            fitnessChange = fitnessChange,
            fatigueChange = fatigueChange,
            capacityChange = capacityChange,
            fitnessBefore = oldMetrics.fitness,
            fitnessAfter = updatedMetrics.fitness,
            fatigueBefore = oldMetrics.fatigue,
            fatigueAfter = updatedMetrics.fatigue,
            capacityBefore = oldMetrics.dailyCapacity,
            capacityAfter = updatedMetrics.dailyCapacity,
        )

        RunHistoryStore.addRun(runLog)
        InMemoryUserMetricsStore.update(updatedMetrics)

        val snapshot = MetricsSnapshot(
            date = todayLocalDate(),
            fitness = updatedMetrics.fitness,
            fatigue = updatedMetrics.fatigue,
            capacity = updatedMetrics.dailyCapacity,
            sleepScore = updatedMetrics.sleep,
        )
        MetricsHistoryStore.addSnapshot(snapshot)
        return updatedMetrics
    }
}
