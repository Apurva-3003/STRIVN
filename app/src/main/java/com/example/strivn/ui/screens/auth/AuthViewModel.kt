package com.example.strivn.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strivn.network.RetrofitClient
import com.example.strivn.network.StrivnApiService
import com.example.strivn.network.TokenStore
import com.example.strivn.network.models.AuthEmailPasswordRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /**
     * Register stored a JWT but [isAuthenticated] is still false until onboarding
     * persists the profile and calls [acknowledgeSessionAfterProfileSaved].
     * Avoids a frame where the app shows the "profile onboarding" branch before prefs are written.
     */
    val awaitingProfileSaveAfterRegister: Boolean = false,
)

class AuthViewModel(
    private val api: StrivnApiService = RetrofitClient.retrofit.create(StrivnApiService::class.java),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isAuthenticated =
        MutableStateFlow(!TokenStore.token.isNullOrBlank())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun forceLogout() {
        TokenStore.token = null
        _isAuthenticated.value = false
        _uiState.value = AuthUiState()
    }

    /** Call after [OnboardingPreferences.completeOnboarding] so root nav never reads stale prefs. */
    fun acknowledgeSessionAfterProfileSaved() {
        if (!TokenStore.token.isNullOrBlank()) {
            _isAuthenticated.value = true
        }
        _uiState.update { it.copy(awaitingProfileSaveAfterRegister = false) }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = api.register(AuthEmailPasswordRequest(email.trim(), password))
                TokenStore.token = response.accessToken
                _uiState.value = AuthUiState(awaitingProfileSaveAfterRegister = true)
            } catch (e: HttpException) {
                val detail = e.response()?.errorBody()?.string()?.trim().orEmpty()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = detail.ifBlank { e.message ?: "Registration failed" },
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Registration failed",
                    )
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = api.login(AuthEmailPasswordRequest(email.trim(), password))
                TokenStore.token = response.accessToken
                _isAuthenticated.value = true
                _uiState.value = AuthUiState()
            } catch (e: HttpException) {
                val detail = e.response()?.errorBody()?.string()?.trim().orEmpty()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = detail.ifBlank { e.message ?: "Login failed" },
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Login failed",
                    )
                }
            }
        }
    }
}
