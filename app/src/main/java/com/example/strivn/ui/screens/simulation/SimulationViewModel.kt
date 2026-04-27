package com.example.strivn.ui.screens.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strivn.core.engines.ObservationEngine
import com.example.strivn.core.engines.RecommendationEngine
import com.example.strivn.core.engines.SimulationEngine
import com.example.strivn.data.repository.InMemoryCheckInStore
import com.example.strivn.data.repository.InMemoryUserMetricsStore
import com.example.strivn.data.repository.InMemoryUserProfileStore
import com.example.strivn.data.repository.RunHistoryStore
import com.example.strivn.data.repository.TrainingRepository
import com.example.strivn.data.repository.TrainingRepositoryProvider
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * MVVM: single [SimulationUiState] exposed via [state], fed by [InMemoryUserMetricsStore.metricsFlow]
 * and [RecommendationEngine] (same inputs as Home).
 */
class SimulationViewModel : ViewModel() {

    private val trainingRepository: TrainingRepository = TrainingRepositoryProvider.instance

    private val _state = MutableStateFlow(SimulationUiState())
    val state: StateFlow<SimulationUiState> = _state.asStateFlow()

    private enum class LastSimulationKind {
        Recommended,
        Custom,
        Skip,
    }

    private var lastSimulationKind: LastSimulationKind? = null

    init {
        combine(
            InMemoryUserMetricsStore.metricsFlow,
            InMemoryUserProfileStore.profileFlow,
            RunHistoryStore.runsFlow,
        ) { metrics, profile, runs ->
            val today = LocalDate.now()
            val hasRunLoggedToday = runs.any { it.date == today }
            val observation = ObservationEngine.evaluate(metrics)
            val recommendation = RecommendationEngine.generateRecommendation(
                profile,
                metrics,
                observation,
            )
            Triple(metrics, recommendation, hasRunLoggedToday)
        }
            .onEach { (metrics, recommendation, hasRunLoggedToday) ->
                val prev = _state.value
                val recPreview = SimulationEngine.simulateRun(
                    currentMetrics = metrics,
                    distanceKm = recommendation.distanceKm,
                    durationMinutes = recommendation.durationMin,
                    rpe = recommendation.rpe,
                )
                val customDistanceKm = prev.customDistance.toDoubleOrNull() ?: 0.0
                val customDurationMin = prev.customDuration.toIntOrNull() ?: 0
                val customRpe = prev.customRpe.toIntOrNull()
                    ?: recommendation.rpe
                val customPreview = SimulationEngine.simulateRun(
                    currentMetrics = metrics,
                    distanceKm = customDistanceKm,
                    durationMinutes = customDurationMin,
                    rpe = customRpe,
                )
                val skipPreview = SimulationEngine.simulateSkipRun(currentMetrics = metrics)
                _state.value = prev.copy(
                    metrics = metrics,
                    recommendation = recommendation,
                    hasRunLoggedToday = hasRunLoggedToday,
                    hasCheckInToday = InMemoryCheckInStore.hasCheckInForToday(),
                    recommendedPreview = if (hasRunLoggedToday) null else recPreview,
                    customPreview = if (hasRunLoggedToday) null else customPreview,
                    skipPreview = if (hasRunLoggedToday) null else skipPreview,
                    showImpactPopup = if (hasRunLoggedToday) false else prev.showImpactPopup,
                    simulationResult = if (hasRunLoggedToday) null else prev.simulationResult,
                    impactShowsLogRun = if (hasRunLoggedToday) true else prev.impactShowsLogRun,
                )
            }
            .launchIn(viewModelScope)
    }

    private fun simulationsAllowed(): Boolean =
        !_state.value.hasRunLoggedToday

    fun simulateRecommendedRun() {
        if (!simulationsAllowed()) return
        val result = _state.value.recommendedPreview ?: return
        lastSimulationKind = LastSimulationKind.Recommended
        _state.value = _state.value.copy(
            showImpactPopup = true,
            impactShowsLogRun = true,
            simulationResult = result,
            userMessage = null,
        )
    }

    fun simulateCustomRun() {
        if (!simulationsAllowed()) return
        val result = _state.value.customPreview ?: return
        lastSimulationKind = LastSimulationKind.Custom
        _state.value = _state.value.copy(
            showImpactPopup = true,
            impactShowsLogRun = true,
            simulationResult = result,
            userMessage = null,
        )
    }

    fun simulateSkipRun() {
        if (!simulationsAllowed()) return
        val result = _state.value.skipPreview ?: return
        lastSimulationKind = LastSimulationKind.Skip
        _state.value = _state.value.copy(
            showImpactPopup = true,
            impactShowsLogRun = false,
            simulationResult = result,
            userMessage = null,
        )
    }

    fun closeImpactPopup() {
        _state.value = _state.value.copy(
            showImpactPopup = false,
            simulationResult = null,
            impactShowsLogRun = true,
        )
    }

    fun dismissUserMessage() {
        _state.value = _state.value.copy(userMessage = null)
    }

    /**
     * Persists the last simulated **run** (recommended or custom) via [trainingRepository.logRun].
     * Skip/rest preview has no Log Run button; recovery for a day without a run is applied by
     * [com.example.strivn.logic.MetricsDayReconciliation] on the next app open.
     */
    fun logRunFromSimulation() {
        val s = _state.value
        if (s.hasRunLoggedToday) return
        if (!InMemoryCheckInStore.hasCheckInForToday()) {
            _state.value = s.copy(userMessage = LOG_RUN_REQUIRES_CHECK_IN)
            return
        }
        if (s.metrics == null) return
        viewModelScope.launch {
            val current = _state.value
            when (lastSimulationKind) {
                null, LastSimulationKind.Skip -> {
                    closeImpactPopup()
                    return@launch
                }
                LastSimulationKind.Recommended -> {
                    val rec = current.recommendation ?: return@launch
                    if (!persistRunToApi(rec.distanceKm, rec.durationMin, rec.rpe)) {
                        _state.value = current.copy(userMessage = LOG_RUN_PERSIST_FAILED)
                        return@launch
                    }
                }
                LastSimulationKind.Custom -> {
                    val distanceKm = current.customDistance.toDoubleOrNull() ?: return@launch
                    val durationMinutes = current.customDuration.toIntOrNull() ?: return@launch
                    val rpe = current.customRpe.toIntOrNull()
                        ?: current.recommendation?.rpe
                        ?: 5
                    if (!persistRunToApi(distanceKm, durationMinutes, rpe)) {
                        _state.value = current.copy(userMessage = LOG_RUN_PERSIST_FAILED)
                        return@launch
                    }
                }
            }
            closeImpactPopup()
        }
    }

    fun updateCustomDistance(value: String) {
        _state.value = _state.value.copy(customDistance = value)
    }

    fun updateCustomDuration(value: String) {
        _state.value = _state.value.copy(customDuration = value)
    }

    fun updateCustomRpe(value: String) {
        _state.value = _state.value.copy(customRpe = value)
    }

    private suspend fun persistRunToApi(distanceKm: Double, durationMinutes: Int, rpe: Int): Boolean =
        trainingRepository.logRun(distanceKm, durationMinutes, rpe).isSuccess

    companion object {
        private const val LOG_RUN_REQUIRES_CHECK_IN =
            "Complete your daily check-in before logging a run."
        private const val LOG_RUN_PERSIST_FAILED = "Could not log run to server."
    }
}
