package com.example.strivn.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strivn.data.models.MetricsSnapshot
import com.example.strivn.data.models.RunLog
import com.example.strivn.data.models.UserMetrics
import com.example.strivn.data.models.UserProfile
import com.example.strivn.data.models.YearWeek
import com.example.strivn.data.models.toYearWeek
import com.example.strivn.data.repository.InMemoryUserMetricsStore
import com.example.strivn.data.repository.InMemoryUserProfileStore
import com.example.strivn.data.repository.MetricsHistoryStore
import com.example.strivn.data.repository.RunHistoryStore
import com.example.strivn.data.repository.TrainingRepository
import com.example.strivn.data.repository.TrainingRepositoryProvider
import com.example.strivn.network.toUserVisibleMessage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val WEEKS_WINDOW = 6
private const val CONSISTENCY_WINDOW_WEEKS = 8
private const val LONG_RUN_WINDOW_DAYS = 30

/**
 * Progress aggregates from [RunHistoryStore], [MetricsHistoryStore], and [InMemoryUserProfileStore].
 * All aggregation runs here; UI observes [state] only.
 */
class ProgressViewModel : ViewModel() {

    private val trainingRepository: TrainingRepository = TrainingRepositoryProvider.instance

    private val _state = MutableStateFlow(ProgressUiState())
    val state: StateFlow<ProgressUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadError = null) }
            val metricsResult = trainingRepository.fetchLatestMetrics()
            val runsResult = trainingRepository.fetchRuns()
            val msgs = listOfNotNull(
                metricsResult.exceptionOrNull()?.toUserVisibleMessage("Couldn’t load metrics."),
                runsResult.exceptionOrNull()?.toUserVisibleMessage("Couldn’t load runs."),
            ).distinct()
            if (msgs.isNotEmpty()) {
                _state.update { it.copy(loadError = msgs.joinToString("\n")) }
            }
            _state.update { it.copy(isLoading = false) }
        }
        combine(
            RunHistoryStore.runsFlow,
            MetricsHistoryStore.metricsHistoryFlow,
            InMemoryUserMetricsStore.metricsFlow,
            InMemoryUserProfileStore.profileFlow,
        ) { runs: List<RunLog>, snapshots: List<MetricsSnapshot>, metrics: UserMetrics, profile: UserProfile? ->
            buildProgressUiState(runs, snapshots, metrics, profile)
        }
            .onEach { built ->
                _state.update { prev ->
                    built.copy(
                        loadError = prev.loadError,
                        isLoading = prev.isLoading,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun dismissLoadError() {
        _state.update { it.copy(loadError = null) }
    }

    private fun buildProgressUiState(
        runs: List<RunLog>,
        snapshots: List<MetricsSnapshot>,
        metrics: UserMetrics,
        profile: UserProfile?,
    ): ProgressUiState {
        val weekKeys = lastSixIsoWeeksOldestFirst()
        val weeklyKm = sumDistanceThisIsoWeek(runs)
        val longest30 = longestRunLastNDaysKm(runs, LONG_RUN_WINDOW_DAYS)
        return ProgressUiState(
            metrics = metrics,
            weeklyDistanceThisWeekKm = weeklyKm,
            longestRunLast30DaysKm = longest30,
            weeklyDistance = aggregateWeeklyDistance(runs, weekKeys),
            fitnessFatigueTrend = aggregateFitnessFatigue(snapshots, weekKeys),
            longRunProgression = aggregateLongRunProgression(runs, weekKeys),
            consistency = aggregateConsistency(runs),
        )
    }

    private fun aggregateWeeklyDistance(
        runs: List<RunLog>,
        weekKeys: List<YearWeek>,
    ): List<WeeklyDistance> {
        val sumByWeek = runs.groupBy { it.date.toYearWeek() }
            .mapValues { (_, rs) -> rs.sumOf { it.distanceKm } }
        return weekKeys.mapIndexed { index, yw ->
            WeeklyDistance(
                weekLabel = weekLabel(index),
                distanceKm = sumByWeek[yw] ?: 0.0,
            )
        }
    }

    private fun aggregateFitnessFatigue(
        snapshots: List<MetricsSnapshot>,
        weekKeys: List<YearWeek>,
    ): List<FitnessFatiguePoint> {
        val byWeek = snapshots.groupBy { it.date.toYearWeek() }
        return weekKeys.mapIndexed { index, yw ->
            val lastInWeek = byWeek[yw].orEmpty().maxByOrNull { it.date }
            FitnessFatiguePoint(
                weekLabel = weekLabel(index),
                fitness = lastInWeek?.fitness ?: 0,
                fatigue = lastInWeek?.fatigue ?: 0,
            )
        }
    }

    private fun aggregateLongRunProgression(
        runs: List<RunLog>,
        weekKeys: List<YearWeek>,
    ): List<WeeklyLongRun> {
        val maxByWeek = runs.groupBy { it.date.toYearWeek() }
            .mapValues { (_, rs) -> rs.maxOfOrNull { it.distanceKm } ?: 0.0 }
        return weekKeys.mapIndexed { index, yw ->
            WeeklyLongRun(
                weekLabel = weekLabel(index),
                distanceKm = maxByWeek[yw] ?: 0.0,
            )
        }
    }

    private fun aggregateConsistency(runs: List<RunLog>): WeeklyConsistency {
        val today = LocalDate.now()
        val mondayThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekStarts = (0 until CONSISTENCY_WINDOW_WEEKS).map { weeksBefore ->
            mondayThisWeek.minusWeeks(weeksBefore.toLong()).toYearWeek()
        }.toSet()
        val weeksWithRun = runs
            .map { it.date.toYearWeek() }
            .filter { it in weekStarts }
            .toSet()
            .size
        val percent = ((weeksWithRun.toDouble() / CONSISTENCY_WINDOW_WEEKS.toDouble()) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)
        return WeeklyConsistency(
            weeksWithRun = weeksWithRun,
            windowWeeks = CONSISTENCY_WINDOW_WEEKS,
            percent = percent,
        )
    }

    private fun sumDistanceThisIsoWeek(runs: List<RunLog>): Double {
        val thisWeek = LocalDate.now().toYearWeek()
        return runs
            .filter { it.date.toYearWeek() == thisWeek }
            .sumOf { it.distanceKm }
    }

    private fun longestRunLastNDaysKm(runs: List<RunLog>, days: Int): Double {
        if (days <= 0) return 0.0
        val cutoff = LocalDate.now().minusDays(days.toLong())
        return runs
            .filter { it.date >= cutoff }
            .maxOfOrNull { it.distanceKm }
            ?: 0.0
    }

    private fun weekLabel(zeroBasedIndex: Int): String = "W${zeroBasedIndex + 1}"

    /**
     * The six ISO weeks ending with the current week, oldest first (W1 … W6).
     */
    private fun lastSixIsoWeeksOldestFirst(): List<YearWeek> {
        val today = LocalDate.now()
        val mondayThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        // Always render oldest → newest so W1 is the oldest and W6 is the current week.
        // (Avoids confusion where the newest week appears first.)
        val newestFirst = (0 until WEEKS_WINDOW).map { weeksAgo ->
            mondayThisWeek.minusWeeks(weeksAgo.toLong()).toYearWeek()
        }
        return newestFirst.asReversed()
    }
}
