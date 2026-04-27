package com.example.strivn.core.engines

import androidx.compose.ui.graphics.Color
import com.example.strivn.data.models.Observation
import com.example.strivn.data.models.UserMetrics

/**
 * Pure Kotlin readiness classification from [UserMetrics] (all gauges use 0–100 = wheel %).
 * [Observation.color] uses fixed ARGB values (aligned with app theme) so this stays independent of UI modules.
 *
 * Evaluation order (first match wins): Primed → High Strain → Under-Recovered → Recovering → Stable → Maintenance.
 */
object ObservationEngine {

    fun evaluate(metrics: UserMetrics): Observation {
        val capacity = metrics.dailyCapacity
        val fatigue = metrics.fatigue
        val sleepScore = metrics.sleep
        val fitness = metrics.fitness

        return when {
            capacity > 75 && fatigue < 45 && fitness >= 35 -> Observation(
                state = "Primed for Progress",
                explanation = "Daily capacity is high, fatigue is controlled, and fitness is solid—favorable conditions for productive training.",
                color = ColorAccent,
            )

            fatigue > 75 -> Observation(
                state = "High Strain",
                explanation = "Fatigue is very elevated; ease intensity and prioritize recovery before hard sessions.",
                color = ColorError,
            )

            fitness < 30 -> Observation(
                state = "Building Base",
                explanation = "Fitness is still developing; keep volume easy and consistent until your aerobic base catches up.",
                color = ColorWarning,
            )

            sleepScore < 50 -> Observation(
                state = "Under-Recovered",
                explanation = "Sleep-based recovery is low; protect adaptation with rest, sleep, and light movement.",
                color = ColorError,
            )

            capacity < 38 -> Observation(
                state = "Recovering",
                explanation = "Readiness is limited; keep training easy and allow capacity to rebuild.",
                color = ColorError,
            )

            capacity in 55..75 && fatigue <= 65 -> Observation(
                state = "Stable & Building",
                explanation = "Training load and recovery are balanced.",
                color = ColorWarning,
            )

            else -> Observation(
                state = "Maintenance Mode",
                explanation = "Hold steady with sustainable volume and consistent habits while readiness stabilizes.",
                color = ColorWarning,
            )
        }
    }

    // Matches Strivn theme accents without depending on ui.theme
    private val ColorAccent = Color(0xFF14B8A6)
    private val ColorWarning = Color(0xFFF59E0B)
    private val ColorError = Color(0xFFF97373)
}
