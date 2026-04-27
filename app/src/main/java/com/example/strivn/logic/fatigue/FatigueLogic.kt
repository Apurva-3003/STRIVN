package com.example.strivn.logic.fatigue

import com.example.strivn.data.models.TrainingSession

object FatigueLogic {
    fun estimateFatigueScore(sessions: List<TrainingSession>): Double {
        // Placeholder: real implementation will likely be based on load/volume over time.
        return sessions.size.toDouble()
    }
}

