package com.example.strivn.data.repository

import android.content.Context
import com.example.strivn.data.models.MetricsSnapshot
import com.example.strivn.data.models.RunLog
import com.example.strivn.data.models.TrainingSession

/**
 * Remote training data backed by the STRIVN FastAPI service.
 * All mutating / network operations return [Result] for ViewModel error handling.
 */
interface TrainingRepository {

    suspend fun getRecentSessions(limit: Int = 30): Result<List<TrainingSession>>

    suspend fun logRun(
        distanceKm: Double,
        durationMinutes: Int,
        rpe: Int,
    ): Result<MetricsSnapshot>

    suspend fun logCheckIn(
        sleep: Double,
        soreness: Int,
        energy: Int,
        stress: Int,
    ): Result<MetricsSnapshot>

    suspend fun fetchRuns(): Result<List<RunLog>>

    suspend fun fetchLatestMetrics(): Result<MetricsSnapshot>

    /**
     * After login/register: load latest server metrics; if none (404), seed via [logCheckIn]
     * using [OnboardingPreferences] profile (or neutral defaults) so backend and client align.
     */
    suspend fun ensureMetricsSeededAfterAuth(context: Context): Result<Unit>
}
