package com.example.strivn.data.models

import java.time.LocalDate

/**
 * Point-in-time [UserMetrics] slice for trend charts (e.g. Progress).
 */
data class MetricsSnapshot(
    val date: LocalDate,
    val fitness: Int,
    val fatigue: Int,
    val capacity: Int,
    val sleepScore: Int,
)
