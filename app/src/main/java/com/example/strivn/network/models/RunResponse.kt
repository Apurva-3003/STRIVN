package com.example.strivn.network.models

import com.google.gson.annotations.SerializedName

/** One run from `GET /api/runs` (matches FastAPI `RunResponse` fields used by the client). */
data class RunResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("distance_km") val distanceKm: Double,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("rpe") val rpe: Int,
    @SerializedName("date") val date: String,
)
