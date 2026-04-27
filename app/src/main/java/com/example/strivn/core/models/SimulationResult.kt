package com.example.strivn.core.models

/**
 * Predicted metrics after simulating a run or rest day.
 *
 * [fitnessChange], [fatigueChange], and [capacityChange] are raw stored-metric deltas (0–100 scale).
 *
 * [wheelFitnessChange], [wheelFatigueChange], [wheelCapacityChange] match Home metric wheels:
 * stored 0–100 maps 1:1 to arc % for all gauges.
 */
data class SimulationResult(
    val beforeFitness: Int,
    val beforeFatigue: Int,
    val beforeCapacity: Int,
    val predictedFitness: Int,
    val predictedFatigue: Int,
    val predictedCapacity: Int,
    val fitnessChange: Int,
    val fatigueChange: Int,
    val capacityChange: Int,
    val wheelFitnessChange: Int,
    val wheelFatigueChange: Int,
    val wheelCapacityChange: Int,
)
