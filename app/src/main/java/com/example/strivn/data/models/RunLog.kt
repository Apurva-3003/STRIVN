package com.example.strivn.data.models

import java.time.LocalDate

/**
 * Logged run entry. At most one run per calendar day in [RunHistoryStore].
 *
 * [fitnessChange], [fatigueChange], and [capacityChange] are raw stored-metric deltas
 * (0–100 scale) from before vs after [com.example.strivn.core.engines.TrainingModel.updateFromRun].
 *
 * [fitnessBefore]/[fitnessAfter], [fatigueBefore]/[fatigueAfter], and [capacityBefore]/[capacityAfter]
 * capture metric values immediately before and after the run (capacity = daily training capacity 0–100).
 */
data class RunLog(
    val date: LocalDate,
    val distanceKm: Double,
    val durationMinutes: Int,
    val rpe: Int,
    /** Explicit deltas for UI; defaults to 0 when unknown (e.g. runs fetched without metric history). */
    val fitnessDelta: Int = 0,
    val fatigueDelta: Int = 0,
    val capacityDelta: Int = 0,
    val fitnessChange: Int,
    val fatigueChange: Int,
    val capacityChange: Int,
    val fitnessBefore: Int,
    val fitnessAfter: Int,
    val fatigueBefore: Int,
    val fatigueAfter: Int,
    val capacityBefore: Int,
    val capacityAfter: Int,
)
