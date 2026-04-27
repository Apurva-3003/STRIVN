package com.example.strivn.ui.screens.progress

data class WeeklyDistance(
    val weekLabel: String,
    val distanceKm: Double,
)

data class FitnessFatiguePoint(
    val weekLabel: String,
    val fitness: Int,
    val fatigue: Int,
)

data class WeeklyLongRun(
    val weekLabel: String,
    val distanceKm: Double,
)

data class WeeklyConsistency(
    /** Weeks with at least one run within the last [windowWeeks] window. */
    val weeksWithRun: Int,
    val windowWeeks: Int,
    /** Consistency percentage = weeksWithRun / windowWeeks * 100. */
    val percent: Int,
)
