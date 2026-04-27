package com.example.strivn.network.models

import com.google.gson.annotations.SerializedName

/** Body for `POST /api/checkin` (matches FastAPI `CheckinSubmit`). */
data class CheckinSubmitRequest(
    @SerializedName("sleep") val sleep: Double,
    @SerializedName("soreness") val soreness: Int,
    @SerializedName("energy") val energy: Int,
    @SerializedName("stress") val stress: Int,
)
