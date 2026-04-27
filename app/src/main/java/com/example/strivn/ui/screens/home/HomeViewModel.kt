package com.example.strivn.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strivn.data.models.DailyCheckIn
import com.example.strivn.data.models.RunLog
import com.example.strivn.data.models.RunRecommendation
import com.example.strivn.data.models.UserMetrics
import com.example.strivn.data.models.UserProfile
import com.example.strivn.data.repository.InMemoryCheckInStore
import com.example.strivn.data.repository.InMemoryUserMetricsStore
import com.example.strivn.data.repository.InMemoryUserProfileStore
import com.example.strivn.data.repository.RunHistoryStore
import com.example.strivn.data.repository.TrainingRepository
import com.example.strivn.data.repository.TrainingRepositoryProvider
import com.example.strivn.network.toUserVisibleMessage
import com.example.strivn.core.engines.ObservationEngine
import com.example.strivn.core.engines.RecommendationEngine
import com.example.strivn.ui.components.CheckInImpactData
import com.example.strivn.ui.components.RunImpactData
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Single source of truth for HomeScreen.
 * Exposes metrics, observation, and recommendation derived from stores.
 *
 * Check-ins call [submitCheckIn]; runs use [logRun]. Both call [TrainingRepository] (API), then metricsFlow emits.
 * [state] recomputes observation and recommendation on each metrics emission.
 */
data class HomeUiState(
    val metrics: UserMetrics,
    val observation: com.example.strivn.data.models.Observation,
    val recommendation: RunRecommendation,
    val hasCheckInToday: Boolean,
    /** True once user has opened the check-in screen today (hides Home banner even before submit). */
    val hasOpenedCheckInToday: Boolean,
    val hasRunLoggedToday: Boolean,
    val showLogRunPopup: Boolean = false,
    val showNiceWorkPopup: Boolean = false,
    val impactDataForNiceWork: RunImpactData? = null,
    val showCheckInImpactPopup: Boolean = false,
    val checkInImpactData: CheckInImpactData? = null,
    /** Shown when user tries to log a run before daily check-in (dismiss with [HomeViewModel.dismissUserMessage]). */
    val userMessage: String? = null,
    /** Initial [TrainingRepository.fetchLatestMetrics] / [TrainingRepository.fetchRuns] or other sync issues. */
    val syncError: String? = null,
)

private data class HomeOverlayState(
    val showLogRun: Boolean,
    val showNiceWork: Boolean,
    val niceWorkImpact: RunImpactData?,
    val showCheckInImpact: Boolean,
    val checkInImpact: CheckInImpactData?,
)

class HomeViewModel : ViewModel() {

    private val trainingRepository: TrainingRepository = TrainingRepositoryProvider.instance

    private val _showLogRunPopup = MutableStateFlow(false)
    private val _showNiceWorkPopup = MutableStateFlow(false)
    private val _impactDataForNiceWork = MutableStateFlow<RunImpactData?>(null)
    private val _showCheckInImpactPopup = MutableStateFlow(false)
    private val _checkInImpactData = MutableStateFlow<CheckInImpactData?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _syncError = MutableStateFlow<String?>(null)

    private val metricsProfileRuns = combine(
        InMemoryUserMetricsStore.metricsFlow,
        InMemoryUserProfileStore.profileFlow,
        RunHistoryStore.runsFlow,
    ) { metrics: UserMetrics, profile: UserProfile?, runs: List<RunLog> ->
        Triple(metrics, profile, runs)
    }

    private val overlayFlows = combine(
        _showLogRunPopup,
        _showNiceWorkPopup,
        _impactDataForNiceWork,
        _showCheckInImpactPopup,
        _checkInImpactData,
    ) { showLogRun, showNiceWork, niceImpact, showCheckInImpact, checkInImpact ->
        HomeOverlayState(
            showLogRun = showLogRun,
            showNiceWork = showNiceWork,
            niceWorkImpact = niceImpact,
            showCheckInImpact = showCheckInImpact,
            checkInImpact = checkInImpact,
        )
    }

    val state: StateFlow<HomeUiState> = combine(
        metricsProfileRuns,
        overlayFlows,
        _userMessage,
        _syncError,
    ) { triple: Triple<UserMetrics, UserProfile?, List<RunLog>>, overlays: HomeOverlayState, userMessage: String?, syncError: String? ->
        val (metrics, profile, runs) = triple
        val observation = ObservationEngine.evaluate(metrics)
        val today = LocalDate.now()
        HomeUiState(
            metrics = metrics,
            observation = observation,
            recommendation = RecommendationEngine.generateRecommendation(profile, metrics, observation),
            hasCheckInToday = InMemoryCheckInStore.hasCheckInForToday(),
            hasOpenedCheckInToday = InMemoryCheckInStore.hasOpenedCheckInToday(),
            hasRunLoggedToday = runs.any { run -> run.date == today },
            showLogRunPopup = overlays.showLogRun,
            showNiceWorkPopup = overlays.showNiceWork,
            impactDataForNiceWork = overlays.niceWorkImpact,
            showCheckInImpactPopup = overlays.showCheckInImpact,
            checkInImpactData = overlays.checkInImpact,
            userMessage = userMessage,
            syncError = syncError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = run {
            val m = InMemoryUserMetricsStore.metrics
            val p = InMemoryUserProfileStore.profile
            val obs = ObservationEngine.evaluate(m)
            val today = LocalDate.now()
            val runs = RunHistoryStore.runsFlow.value
            HomeUiState(
                metrics = m,
                observation = obs,
                recommendation = RecommendationEngine.generateRecommendation(p, m, obs),
                hasCheckInToday = InMemoryCheckInStore.hasCheckInForToday(),
                hasOpenedCheckInToday = InMemoryCheckInStore.hasOpenedCheckInToday(),
                hasRunLoggedToday = runs.any { r -> r.date == today },
                showLogRunPopup = false,
                showNiceWorkPopup = false,
                impactDataForNiceWork = null,
                showCheckInImpactPopup = false,
                checkInImpactData = null,
                userMessage = null,
                syncError = null,
            )
        },
    )

    init {
        viewModelScope.launch {
            val metricsResult = trainingRepository.fetchLatestMetrics()
            val runsResult = trainingRepository.fetchRuns()
            val parts = listOfNotNull(
                metricsResult.exceptionOrNull()?.toUserVisibleMessage("Couldn’t load metrics."),
                runsResult.exceptionOrNull()?.toUserVisibleMessage("Couldn’t load runs."),
            ).distinct()
            if (parts.isNotEmpty()) {
                _syncError.value = parts.joinToString("\n")
            }
        }
    }

    fun dismissSyncError() {
        _syncError.value = null
    }

    fun openLogRunPopup() {
        if (!InMemoryCheckInStore.hasCheckInForToday()) {
            _userMessage.value = LOG_RUN_REQUIRES_CHECK_IN
            return
        }
        _showLogRunPopup.value = true
    }

    fun dismissUserMessage() {
        _userMessage.value = null
    }

    fun closeLogRunPopup() {
        _showLogRunPopup.value = false
    }

    /**
     * Daily check-in: `POST /api/checkin` via [trainingRepository], then local check-in record for “today” gating.
     * [state] refreshes via [InMemoryUserMetricsStore.metricsFlow].
     */
    fun submitCheckIn(
        checkIn: DailyCheckIn,
        onFinished: (kotlin.Result<Unit>) -> Unit = {},
    ) {
        val profile = InMemoryUserProfileStore.profile
        val beforeMetrics = InMemoryUserMetricsStore.metrics
        val beforeRecommendation = RecommendationEngine.generateRecommendation(
            profile,
            beforeMetrics,
            ObservationEngine.evaluate(beforeMetrics),
        )

        viewModelScope.launch {
            val soreness = checkIn.muscleSoreness.coerceIn(1, 5) * 2
            val energy = checkIn.energyLevel.coerceIn(1, 5) * 2
            val stress = checkIn.stressLevel.coerceIn(1, 5) * 2
            val result = trainingRepository.logCheckIn(
                sleep = checkIn.sleepHours,
                soreness = soreness,
                energy = energy,
                stress = stress,
            )
            result.fold(
                onSuccess = {
                    InMemoryCheckInStore.saveOrUpdate(checkIn)
                    val updatedMetrics = InMemoryUserMetricsStore.metrics
                    val afterObservation = ObservationEngine.evaluate(updatedMetrics)
                    val afterRecommendation = RecommendationEngine.generateRecommendation(
                        profile,
                        updatedMetrics,
                        afterObservation,
                    )
                    _checkInImpactData.value = CheckInImpactData(
                        fitnessChange = updatedMetrics.fitness - beforeMetrics.fitness,
                        fatigueChange = updatedMetrics.fatigue - beforeMetrics.fatigue,
                        sleepChange = updatedMetrics.sleep - beforeMetrics.sleep,
                        dtcChange = updatedMetrics.dailyCapacity - beforeMetrics.dailyCapacity,
                        previousRecommendation = beforeRecommendation,
                        newRecommendation = afterRecommendation,
                    )
                    _showCheckInImpactPopup.value = true
                    onFinished(kotlin.Result.success(Unit))
                },
                onFailure = { e ->
                    onFinished(kotlin.Result.failure(e))
                },
            )
        }
    }

    fun closeCheckInImpactPopup() {
        _showCheckInImpactPopup.value = false
        _checkInImpactData.value = null
    }

    /**
     * `POST /api/runs` via [trainingRepository]; Nice Work popup uses updated store metrics.
     */
    fun logRun(distanceKm: Double, durationMin: Int, effortRpe: Int) {
        if (!InMemoryCheckInStore.hasCheckInForToday()) {
            _userMessage.value = LOG_RUN_REQUIRES_CHECK_IN
            return
        }
        val currentMetrics = InMemoryUserMetricsStore.metrics
        viewModelScope.launch {
            val result = trainingRepository.logRun(
                distanceKm = distanceKm,
                durationMinutes = durationMin,
                rpe = effortRpe,
            )
            result.fold(
                onSuccess = {
                    val updatedMetrics = InMemoryUserMetricsStore.metrics
                    _impactDataForNiceWork.value = RunImpactData(
                        fitnessChange = updatedMetrics.fitness - currentMetrics.fitness,
                        fatigueChange = updatedMetrics.fatigue - currentMetrics.fatigue,
                        projectedTomorrowCapacity = updatedMetrics.dailyCapacity,
                    )
                    _showLogRunPopup.value = false
                    _showNiceWorkPopup.value = true
                },
                onFailure = { e ->
                    _userMessage.value = e.toUserVisibleMessage("Failed to log run. Please try again.")
                },
            )
        }
    }

    fun closeNiceWorkPopup() {
        _showNiceWorkPopup.value = false
        _impactDataForNiceWork.value = null
    }

    companion object {
        const val LOG_RUN_REQUIRES_CHECK_IN =
            "Complete your daily check-in before logging a run."
    }
}
