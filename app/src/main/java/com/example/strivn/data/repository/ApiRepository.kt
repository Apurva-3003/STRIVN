package com.example.strivn.data.repository

import android.content.Context
import android.util.Log
import com.example.strivn.data.models.MetricsSnapshot
import com.example.strivn.data.models.RunLog
import com.example.strivn.data.models.TrainingSession
import com.example.strivn.data.models.UserMetrics
import com.example.strivn.network.StrivnApiService
import com.example.strivn.network.models.CheckinSubmitRequest
import com.example.strivn.network.models.MetricsSnapshotResponse
import com.example.strivn.network.models.RunResponse
import com.example.strivn.network.models.RunSubmitRequest
import java.time.Duration
import java.time.LocalDate
import retrofit2.HttpException

/**
 * [TrainingRepository] backed by [StrivnApiService] (`POST/GET /api/runs`, `POST /api/checkin`,
 * `GET /api/metrics/latest`). All operations return [Result] from `try/catch`. On success, updates
 * [InMemoryUserMetricsStore], [MetricsHistoryStore], and [RunHistoryStore] where applicable.
 */
class ApiRepository(
    private val api: StrivnApiService,
) : TrainingRepository {

    private companion object {
        private const val SEED_LOG_TAG = "STRIVN_SEED"
    }

    override suspend fun getRecentSessions(limit: Int): Result<List<TrainingSession>> =
        try {
            val runs = api.getRuns()
                .sortedByDescending { LocalDate.parse(it.date) }
                .take(limit)
            Result.success(
                runs.map { r ->
                    TrainingSession(
                        date = LocalDate.parse(r.date),
                        duration = Duration.ofMinutes(r.durationMinutes.toLong()),
                    )
                },
            )
        } catch (e: Throwable) {
            Result.failure(e)
        }

    override suspend fun logRun(
        distanceKm: Double,
        durationMinutes: Int,
        rpe: Int,
    ): Result<MetricsSnapshot> =
        try {
            val before = InMemoryUserMetricsStore.metricsFlow.value
            val body = RunSubmitRequest(
                distanceKm = distanceKm,
                durationMinutes = durationMinutes,
                rpe = rpe,
            )
            val resp = api.createRun(body)
            val snapshot = resp.toMetricsSnapshot()
            MetricsHistoryStore.addSnapshot(snapshot)
            InMemoryUserMetricsStore.update(snapshot.toUserMetrics(before.injuryRisk))
            val fitnessDelta = snapshot.fitness - before.fitness
            val fatigueDelta = snapshot.fatigue - before.fatigue
            val capacityDelta = snapshot.capacity - before.dailyCapacity
            val run = RunLog(
                date = snapshot.date,
                distanceKm = distanceKm,
                durationMinutes = durationMinutes,
                rpe = rpe,
                fitnessDelta = fitnessDelta,
                fatigueDelta = fatigueDelta,
                capacityDelta = capacityDelta,
                fitnessChange = fitnessDelta,
                fatigueChange = fatigueDelta,
                capacityChange = capacityDelta,
                fitnessBefore = before.fitness,
                fitnessAfter = snapshot.fitness,
                fatigueBefore = before.fatigue,
                fatigueAfter = snapshot.fatigue,
                capacityBefore = before.dailyCapacity,
                capacityAfter = snapshot.capacity,
            )
            RunHistoryStore.addRun(run)
            Result.success(snapshot)
        } catch (e: Throwable) {
            Result.failure(e)
        }

    override suspend fun logCheckIn(
        sleep: Double,
        soreness: Int,
        energy: Int,
        stress: Int,
    ): Result<MetricsSnapshot> =
        try {
            val body = CheckinSubmitRequest(
                sleep = sleep,
                soreness = soreness,
                energy = energy,
                stress = stress,
            )
            val resp = api.createCheckin(body)
            val snapshot = resp.toMetricsSnapshot()
            applyMetricsSnapshot(snapshot)
            Result.success(snapshot)
        } catch (e: Throwable) {
            Result.failure(e)
        }

    override suspend fun fetchRuns(): Result<List<RunLog>> =
        try {
            val mapped = api.getRuns().map { it.toRunLogPlaceholder() }
            RunHistoryStore.replaceRuns(mapped)
            Result.success(mapped)
        } catch (e: Throwable) {
            Result.failure(e)
        }

    override suspend fun fetchLatestMetrics(): Result<MetricsSnapshot> =
        try {
            val resp = api.getLatestMetrics()
            val snapshot = resp.toMetricsSnapshot()
            applyMetricsSnapshot(snapshot)
            Result.success(snapshot)
        } catch (e: Throwable) {
            Result.failure(e)
        }

    override suspend fun ensureMetricsSeededAfterAuth(context: Context): Result<Unit> {
        Log.d(SEED_LOG_TAG, "ensureMetricsSeededAfterAuth called")
        return try {
            try {
                val resp = api.getLatestMetrics()
                applyMetricsSnapshot(resp.toMetricsSnapshot())
                Log.d(SEED_LOG_TAG, "getLatestMetrics() succeeded; metrics applied")
                Result.success(Unit)
            } catch (e: HttpException) {
                val message = e.message ?: ""
                Log.e(
                    SEED_LOG_TAG,
                    "getLatestMetrics() HttpException code=${e.code()} type=${e::class.java.name} message=$message",
                    e,
                )
                if (e.code() != 404) {
                    Result.failure(e)
                } else {
                    Log.d(SEED_LOG_TAG, "getLatestMetrics() returned 404; seeding via POST /api/checkin")
                    val profile = OnboardingPreferences(context).getProfile()
                    val sleep = profile?.avgSleepHours?.coerceIn(4.0, 10.0) ?: 7.5
                    val soreness = if (profile?.injuryStatus == true) 6 else 5
                    val energy = 5
                    val stress = 5
                    val body = CheckinSubmitRequest(
                        sleep = sleep,
                        soreness = soreness,
                        energy = energy,
                        stress = stress,
                    )
                    Log.d(
                        SEED_LOG_TAG,
                        "Sending seeding check-in sleep=$sleep soreness=$soreness energy=$energy stress=$stress",
                    )
                    try {
                        val resp = api.createCheckin(body)
                        val snapshot = resp.toMetricsSnapshot()
                        applyMetricsSnapshot(snapshot)
                        Log.d(SEED_LOG_TAG, "Seeding check-in succeeded; metrics applied")
                        Log.d(
                            SEED_LOG_TAG,
                            "Seeded metrics: fitness=${snapshot.fitness} fatigue=${snapshot.fatigue} capacity=${snapshot.capacity} sleep=${snapshot.sleepScore}",
                        )
                        Result.success(Unit)
                    } catch (t: Throwable) {
                        Log.e(
                            SEED_LOG_TAG,
                            "Seeding check-in failed type=${t::class.java.name} message=${t.message}",
                            t,
                        )
                        Result.failure(t)
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e(
                SEED_LOG_TAG,
                "ensureMetricsSeededAfterAuth failed type=${e::class.java.name} message=${e.message}",
                e,
            )
            Result.failure(e)
        }
    }

    private fun applyMetricsSnapshot(snapshot: MetricsSnapshot) {
        val injuryRisk = InMemoryUserMetricsStore.metrics.injuryRisk
        MetricsHistoryStore.addSnapshot(snapshot)
        InMemoryUserMetricsStore.update(snapshot.toUserMetrics(injuryRisk))
    }
}

private fun MetricsSnapshotResponse.toMetricsSnapshot(): MetricsSnapshot =
    MetricsSnapshot(
        date = LocalDate.parse(date),
        fitness = fitness,
        fatigue = fatigue,
        capacity = capacity,
        sleepScore = sleepScore,
    )

private fun MetricsSnapshot.toUserMetrics(injuryRisk: Int): UserMetrics =
    UserMetrics(
        fitness = fitness,
        fatigue = fatigue,
        sleep = sleepScore,
        dailyCapacity = capacity,
        injuryRisk = injuryRisk,
    )

/** GET /api/runs payload has no deltas; zeros keep list views working until the API grows. */
private fun RunResponse.toRunLogPlaceholder(): RunLog {
    val loggedDate = LocalDate.parse(date)
    return RunLog(
        date = loggedDate,
        distanceKm = distanceKm,
        durationMinutes = durationMinutes,
        rpe = rpe,
        fitnessChange = 0,
        fatigueChange = 0,
        capacityChange = 0,
        fitnessBefore = 0,
        fitnessAfter = 0,
        fatigueBefore = 0,
        fatigueAfter = 0,
        capacityBefore = 0,
        capacityAfter = 0,
    )
}
