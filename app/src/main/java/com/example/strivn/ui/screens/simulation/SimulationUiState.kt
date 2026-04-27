package com.example.strivn.ui.screens.simulation

import com.example.strivn.core.engines.Recommendation
import com.example.strivn.core.models.SimulationResult
import com.example.strivn.data.models.UserMetrics

data class SimulationUiState(
    val metrics: UserMetrics? = null,
    val recommendation: Recommendation? = null,

    /** True if a run is already logged for today (any source). Simulations and logging are blocked. */
    val hasRunLoggedToday: Boolean = false,

    val hasCheckInToday: Boolean = false,

    val showImpactPopup: Boolean = false,
    /** False for “skip/rest” simulation — no Log Run in the dialog (preview only). */
    val impactShowsLogRun: Boolean = true,
    val simulationResult: SimulationResult? = null,

    val customDistance: String = "",
    val customDuration: String = "",
    val customRpe: String = "",

    val recommendedPreview: SimulationResult? = null,
    val customPreview: SimulationResult? = null,
    val skipPreview: SimulationResult? = null,

    /** Error when logging a run without completing check-in (see [SimulationViewModel.dismissUserMessage]). */
    val userMessage: String? = null,
)
