package com.example.strivn.data.repository

import com.example.strivn.data.models.UserMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for UserMetrics. Exposes [metricsFlow] for ViewModel observation.
 *
 * Data flow:
 * 1. Check-in / run logged via [TrainingRepository] (API) → [update] stores server metrics
 * 2. ViewModel collects [metricsFlow] → exposes to UI
 *
 * Initial values align with the FastAPI backend default baseline before first snapshot.
 */
object InMemoryUserMetricsStore {
    private val _metricsFlow = MutableStateFlow(
        UserMetrics(
            fitness = 30,
            fatigue = 20,
            sleep = 60,
            dailyCapacity = 60,
            injuryRisk = 0,
        ),
    )
    val metricsFlow: StateFlow<UserMetrics> = _metricsFlow.asStateFlow()

    var metrics: UserMetrics
        get() = _metricsFlow.value
        private set(value) {
            _metricsFlow.value = value
        }

    fun update(newMetrics: UserMetrics) {
        metrics = newMetrics
    }
}

