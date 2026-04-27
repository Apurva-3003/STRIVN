package com.example.strivn.data.repository

import androidx.compose.ui.graphics.Color
import com.example.strivn.data.models.Observation
import com.example.strivn.data.models.RunRecommendation
import com.example.strivn.data.models.UserMetrics

/**
 * Static demo payloads only. The app uses [TrainingRepositoryProvider.instance]
 * ([ApiRepository] + [StrivnApiService]) for real data; keep this for tests or previews if needed.
 */
object FakeRepository {
    fun getUserMetrics(): UserMetrics {
        return UserMetrics(
            fatigue = 65,
            fitness = 72,
            sleep = 80,
            dailyCapacity = 78,
            injuryRisk = 25,
        )
    }

    fun getRunRecommendation(): RunRecommendation {
        return RunRecommendation(
            type = "Tempo Run",
            focus = "Aerobic Threshold",
            environment = "Outdoor",
            rpe = 6,
            distanceKm = 5.0,
            durationMin = 42,
            injuryRisk = 32,
        )
    }

    fun getObservation(): Observation {
        return Observation(
            state = "Stable & Building",
            explanation = "Fatigue is moderate while fitness remains stable. Sleep recovery is strong, making today suitable for moderate aerobic work.",
            color = Color(0xFFF59E0B), // yellow/orange (moderate readiness)
        )
    }
}

