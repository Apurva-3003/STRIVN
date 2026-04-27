package com.example.strivn.data.repository

import com.example.strivn.data.models.RunLog
import java.time.LocalDate

/**
 * Backwards-compatible facade over [RunHistoryStore] (daily run log, one entry per day).
 */
object InMemoryRunLogStore {

    /** Returns the run log for today if one exists. */
    fun getRunLogForToday(): RunLog? = RunHistoryStore.getRunForToday()

    /** Returns true if a run has been logged for today. */
    fun hasRunLoggedToday(): Boolean = getRunLogForToday() != null

    /** Saves or updates the run log for the given date. Replaces any existing one for that date. */
    fun saveOrUpdate(runLog: RunLog) {
        RunHistoryStore.addRun(runLog)
    }

    /**
     * Returns run logs from the last N calendar days (including today when present).
     */
    fun getRecentRunLogs(days: Int = 7): List<RunLog> {
        val today = LocalDate.now()
        val start = today.minusDays((days - 1).coerceAtLeast(0).toLong())
        return RunHistoryStore.runsFlow.value
            .filter { it.date in start..today }
            .sortedByDescending { it.date }
    }

    /**
     * Sum of (duration * RPE) for runs in the last 7 days.
     * Proxy for acute training load.
     */
    fun getWeeklyTrainingLoad(): Double = getRecentRunLogs(7).sumOf { run ->
        run.durationMinutes * run.rpe
    }.toDouble()
}
