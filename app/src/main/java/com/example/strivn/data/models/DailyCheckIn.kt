package com.example.strivn.data.models

/**
 * Morning recovery check-in. One per day maximum.
 *
 * @param date yyyy-MM-dd format for storage/comparison
 * @param sleepHours Hours slept (0–12)
 * @param sleepQuality 1–5 (1=poor, 5=excellent)
 * @param muscleSoreness 1–5 (1=none, 5=severe)
 * @param energyLevel 1–5 (1=low, 5=high)
 * @param stressLevel 1–5 (1=low, 5=high)
 */
data class DailyCheckIn(
    val date: String,
    val sleepHours: Double,
    val sleepQuality: Int,
    val muscleSoreness: Int,
    val energyLevel: Int,
    val stressLevel: Int,
)
