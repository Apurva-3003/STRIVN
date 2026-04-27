package com.example.strivn.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Simple global auth event stream. Used to broadcast "logout now" from networking code (e.g. 401s).
 */
object AuthEventBus {
    private val _logoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    suspend fun sendLogout() {
        _logoutEvent.emit(Unit)
    }

    fun trySendLogout(): Boolean = _logoutEvent.tryEmit(Unit)
}

