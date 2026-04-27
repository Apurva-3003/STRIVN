package com.example.strivn.network

import retrofit2.HttpException

/** Short message for dialogs / banners (FastAPI detail body when present). */
fun Throwable.toUserVisibleMessage(fallback: String = "Something went wrong. Please try again."): String {
    if (this is HttpException) {
        val detail = response()?.errorBody()?.string()?.trim().orEmpty()
        if (detail.isNotBlank()) return detail
        return message?.takeIf { it.isNotBlank() }
            ?: "Request failed (${code()})."
    }
    return message?.takeIf { it.isNotBlank() } ?: fallback
}
