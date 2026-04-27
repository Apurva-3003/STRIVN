package com.example.strivn.ui.screens.progress

import com.example.strivn.data.models.UserMetrics

data class ProgressUiState(
    val isLoading: Boolean = false,
    val metrics: UserMetrics? = null,
    /** Sum of run distances within the current ISO week. */
    val weeklyDistanceThisWeekKm: Double = 0.0,
    /** Longest single run (km) within the last 30 days. */
    val longestRunLast30DaysKm: Double = 0.0,
    val weeklyDistance: List<WeeklyDistance> = emptyList(),
    val fitnessFatigueTrend: List<FitnessFatiguePoint> = emptyList(),
    val longRunProgression: List<WeeklyLongRun> = emptyList(),
    val consistency: WeeklyConsistency? = null,
    val loadError: String? = null,
)
