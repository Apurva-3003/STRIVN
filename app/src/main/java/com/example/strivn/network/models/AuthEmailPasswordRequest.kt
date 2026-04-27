package com.example.strivn.network.models

import com.google.gson.annotations.SerializedName

/** Body for `POST /api/auth/register` and `POST /api/auth/login`. */
data class AuthEmailPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
)
