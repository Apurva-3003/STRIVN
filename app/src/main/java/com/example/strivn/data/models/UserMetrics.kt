package com.example.strivn.data.models

import androidx.compose.ui.graphics.Color

data class UserMetrics(
    val fatigue: Int,           // 0–100
    val fitness: Int,           // 0–100
    val sleep: Int,             // 0–100
    val dailyCapacity: Int,     // 0–100
    val injuryRisk: Int = 0,    // 0–100 (heuristic from recommendation / UI)
)

data class RunRecommendation(
    val type: String,
    val focus: String,
    val environment: String,
    val rpe: Int,
    val distanceKm: Double,
    val durationMin: Int,
    val injuryRisk: Int         // 0–100
)

data class Observation(
    val state: String,
    val explanation: String,
    val color: Color
)

