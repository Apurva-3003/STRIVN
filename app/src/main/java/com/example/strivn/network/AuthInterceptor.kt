package com.example.strivn.network

import okhttp3.Interceptor
import okhttp3.Response

/** Adds `Authorization: Bearer <token>` when [TokenStore.token] is non-null and non-blank. */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val jwt = TokenStore.token?.trim().orEmpty()
        if (jwt.isEmpty()) {
            return chain.proceed(original)
        }
        val authenticated = original.newBuilder()
            .header("Authorization", "Bearer $jwt")
            .build()
        return chain.proceed(authenticated)
    }
}
