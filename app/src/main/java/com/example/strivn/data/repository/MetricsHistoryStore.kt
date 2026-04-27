package com.example.strivn.data.repository

import com.example.strivn.data.models.MetricsSnapshot
import com.example.strivn.data.models.RunLog
import com.example.strivn.data.models.YearWeek
import com.example.strivn.data.models.toYearWeek
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory daily [MetricsSnapshot] history for the current session (trends / Progress).
 *
 * Singleton + [MutableStateFlow], pure Kotlin (no Android APIs).
 */
object MetricsHistoryStore {

    private val _metricsHistoryFlow = MutableStateFlow<List<MetricsSnapshot>>(emptyList())
    val metricsHistoryFlow: StateFlow<List<MetricsSnapshot>> = _metricsHistoryFlow.asStateFlow()

    val history: List<MetricsSnapshot>
        get() = _metricsHistoryFlow.value

    /**
     * Appends [snapshot] and keeps the list sorted by [MetricsSnapshot.date] ascending.
     */
    fun addSnapshot(snapshot: MetricsSnapshot) {
        _metricsHistoryFlow.value = (history + snapshot).sortedBy { it.date }
    }

    /** Entries with [MetricsSnapshot.date] on or after [LocalDate.now] minus 30 days. */
    fun getLast30Days(): List<MetricsSnapshot> {
        val cutoff = LocalDate.now().minusDays(30)
        return history
            .filter { it.date >= cutoff }
            .sortedBy { it.date }
    }

    /**
     * Entries from the Monday starting the window of the last [weeks] ISO calendar weeks through today
     * (inclusive). Week boundaries: Monday–Sunday per ISO convention.
     */
    fun getLastNWeeks(weeks: Int): List<MetricsSnapshot> {
        if (weeks <= 0) return emptyList()
        val today = LocalDate.now()
        val mondayThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val windowStart = mondayThisWeek.minusWeeks((weeks - 1).toLong())
        return history
            .filter { it.date >= windowStart && it.date <= today }
            .sortedBy { it.date }
    }

    fun clear() {
        _metricsHistoryFlow.value = emptyList()
    }

    /**
     * Total [RunLog.distanceKm] per ISO calendar week.
     */
    fun getWeeklyDistance(runs: List<RunLog>): Map<YearWeek, Double> =
        runs.groupBy { it.date.toYearWeek() }
            .mapValues { (_, weekRuns) -> weekRuns.sumOf { it.distanceKm } }
            .toSortedMap()

    /**
     * Longest single run (max [RunLog.distanceKm]) per ISO calendar week.
     */
    fun getWeeklyLongRun(runs: List<RunLog>): Map<YearWeek, Double> =
        runs.groupBy { it.date.toYearWeek() }
            .mapValues { (_, weekRuns) -> weekRuns.maxOf { it.distanceKm } }
            .toSortedMap()

    /** Number of runs logged per ISO calendar week. */
    fun getWeeklyRunCount(runs: List<RunLog>): Map<YearWeek, Int> =
        runs.groupBy { it.date.toYearWeek() }
            .mapValues { (_, weekRuns) -> weekRuns.size }
            .toSortedMap()
}
