package com.example.strivn.network.models

import com.google.gson.annotations.SerializedName

/** Body for `POST /api/runs` (matches FastAPI `RunSubmit`). */
data class RunSubmitRequest(
    @SerializedName("distance_km") val distanceKm: Double,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("rpe") val rpe: Int,
)
