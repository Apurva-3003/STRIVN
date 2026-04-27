package com.example.strivn.data.repository

/**
 * Lightweight in-memory snapshot of last known fitness/fatigue for optional future use
 * (e.g. load-spike heuristics). Seeded from onboarding or server-synced metrics.
 */
object InMemoryBanisterStateStore {

    private var fitness: Int = 0
    private var fatigue: Int = 0

    /** Seeds state when initializing from onboarding or after syncing server metrics. */
    fun initializeState(fitness: Int, fatigue: Int) {
        this.fitness = fitness.coerceIn(0, 100)
        this.fatigue = fatigue.coerceIn(0, 100)
    }
}
