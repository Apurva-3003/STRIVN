package com.example.strivn.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Run data to pre-populate the Today's Run section when navigating from "Log Run" popup.
 * (No persistence yet.)
 */
data class PendingRunToLog(
    val distanceKm: Double,
    val durationMin: Int,
    val rpe: Int,
)

/**
 * In-memory store for a run pending to be logged.
 * Set when user taps "Log Run" in the workout completion popup; cleared when CheckInScreen reads it.
 */
object PendingRunToLogStore {
    private var _pendingRun by mutableStateOf<PendingRunToLog?>(null)

    fun setPendingRun(run: PendingRunToLog) {
        _pendingRun = run
    }

    /** Returns and clears the pending run. Call from CheckInScreen to pre-populate. */
    fun takePendingRun(): PendingRunToLog? {
        val run = _pendingRun
        _pendingRun = null
        return run
    }
}
