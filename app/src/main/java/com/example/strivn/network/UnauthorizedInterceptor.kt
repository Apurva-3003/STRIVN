package com.example.strivn.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Global 401 handler: clears [TokenStore.token] and emits a logout event so UI can return to login.
 */
class UnauthorizedInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            TokenStore.token = null
            AuthEventBus.trySendLogout()
        }
        return response
    }
}

