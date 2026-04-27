package com.example.strivn.network.models

import com.google.gson.annotations.SerializedName

/** Response from auth endpoints (matches FastAPI `TokenResponse`). */
data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
)
