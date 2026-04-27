package com.example.strivn.network.models

import com.google.gson.annotations.SerializedName

/**
 * Metrics snapshot from run/check-in/latest endpoints
 * (subset of FastAPI `MetricsHistoryResponse`: date + four scores; extra JSON keys are ignored).
 */
data class MetricsSnapshotResponse(
    @SerializedName("fitness") val fitness: Int,
    @SerializedName("fatigue") val fatigue: Int,
    @SerializedName("capacity") val capacity: Int,
    @SerializedName("sleep_score") val sleepScore: Int,
    @SerializedName("date") val date: String,
)
