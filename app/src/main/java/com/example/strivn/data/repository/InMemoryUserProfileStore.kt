package com.example.strivn.data.repository

import com.example.strivn.data.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory store for the user profile from onboarding.
 */
object InMemoryUserProfileStore {
    private val _profileFlow = MutableStateFlow<UserProfile?>(null)
    val profileFlow: StateFlow<UserProfile?> = _profileFlow.asStateFlow()

    var profile: UserProfile?
        get() = _profileFlow.value
        private set(value) {
            _profileFlow.value = value
        }

    fun update(newProfile: UserProfile) {
        profile = newProfile
    }

    /** Clears the profile. Useful for testing or re-onboarding. */
    fun clear() {
        profile = null
    }
}
