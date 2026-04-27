package com.example.strivn.data.repository

import com.example.strivn.data.models.RunLog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory run history for the current user session.
 *
 * Same pattern as [InMemoryUserMetricsStore] / [InMemoryUserProfileStore]: singleton object,
 * [MutableStateFlow] as source of truth, exposed as [runsFlow].
 */
object RunHistoryStore {

    private val _runsFlow = MutableStateFlow<List<RunLog>>(emptyList())
    val runsFlow: StateFlow<List<RunLog>> = _runsFlow.asStateFlow()

    val runs: List<RunLog>
        get() = _runsFlow.value

    /**
     * Adds [run] to history. If a run already exists for [RunLog.date], it is replaced so at most
     * one entry per calendar day remains. The list is kept sorted by date descending (most recent first).
     */
    fun addRun(run: RunLog) {
        val merged = runs.filterNot { it.date == run.date } + run
        _runsFlow.value = merged.sortedByDescending { it.date }
    }

    fun getLastRun(): RunLog? = runs.firstOrNull()

    /** Runs whose [RunLog.date] falls in the inclusive window [today − 6 days, today] (seven calendar days). */
    fun getRunsLast7Days(): List<RunLog> {
        val today = LocalDate.now()
        val start = today.minusDays(6)
        return runs
            .filter { it.date in start..today }
            .sortedByDescending { it.date }
    }

    /** Runs whose date falls in the current ISO calendar week (Monday–Sunday). */
    fun getRunsThisWeek(): List<RunLog> {
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)
        return runs
            .filter { it.date in weekStart..weekEnd }
            .sortedByDescending { it.date }
    }

    fun clear() {
        _runsFlow.value = emptyList()
    }

    /** Replaces all runs (e.g. after `GET /api/runs`). Sorted by date descending. */
    fun replaceRuns(runs: List<RunLog>) {
        // Preserve richer per-run impact data (before/after/deltas) when the same date already exists locally.
        val existingByDate = this.runs.associateBy { it.date }
        val merged = runs.map { incoming ->
            val existing = existingByDate[incoming.date]
            if (existing == null) {
                incoming
            } else {
                incoming.copy(
                    fitnessDelta = existing.fitnessDelta,
                    fatigueDelta = existing.fatigueDelta,
                    capacityDelta = existing.capacityDelta,
                    fitnessChange = existing.fitnessChange,
                    fatigueChange = existing.fatigueChange,
                    capacityChange = existing.capacityChange,
                    fitnessBefore = existing.fitnessBefore,
                    fitnessAfter = existing.fitnessAfter,
                    fatigueBefore = existing.fatigueBefore,
                    fatigueAfter = existing.fatigueAfter,
                    capacityBefore = existing.capacityBefore,
                    capacityAfter = existing.capacityAfter,
                )
            }
        }
        _runsFlow.value = merged.sortedByDescending { it.date }
    }

    /** Most recent run for [date], if any. */
    fun getRunForToday(): RunLog? {
        val today = LocalDate.now()
        return runs.find { it.date == today }
    }
}
