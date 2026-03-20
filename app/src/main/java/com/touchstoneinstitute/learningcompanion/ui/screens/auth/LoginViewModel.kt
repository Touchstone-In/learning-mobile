package com.touchstoneinstitute.learningcompanion.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchstoneinstitute.learningcompanion.data.repository.AuthRepository
import com.touchstoneinstitute.learningcompanion.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val isCheckingSession: Boolean = true,
    /** Set when the auth service requires MFA before issuing tokens. */
    val requiresMfa: Boolean = false,
    /** "App" for TOTP authenticator, "Email" for email-based OTP. */
    val mfaMethod: String? = null,
    /** The email used in the login attempt — needed to verify OTP. */
    val mfaEmail: String? = null,
    /** Set when a likely invited/new account must complete MFA setup in the web portal. */
    val mfaSetupRequired: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkExistingSession()
    }

    /**
     * On app launch, check if a valid session exists.
     */
    private fun checkExistingSession() {
        viewModelScope.launch {
            val hasToken = authRepository.hasStoredToken()
            if (hasToken) {
                when (authRepository.validateSession()) {
                    is AuthResult.Success -> {
                        _uiState.update { it.copy(isCheckingSession = false, isLoggedIn = true) }
                    }
                    is AuthResult.Error, is AuthResult.MfaSetupRequired -> {
                        _uiState.update { it.copy(isCheckingSession = false) }
                    }
                }
            } else {
                _uiState.update { it.copy(isCheckingSession = false) }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    val response = result.data
                    if (response.requiresMfa == true) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                requiresMfa = true,
                                mfaMethod = response.mfaMethod,
                                mfaEmail = email.trim().lowercase()
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    }
                }
                is AuthResult.MfaSetupRequired -> {
                    _uiState.update {
                        it.copy(isLoading = false, mfaSetupRequired = true)
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    /** Called after MFA verification succeeds — resets MFA state and marks logged in. */
    fun onMfaVerified() {
        _uiState.update {
            it.copy(requiresMfa = false, mfaMethod = null, mfaEmail = null, isLoggedIn = true)
        }
    }

    /** Called when user presses back from MFA screen — returns to login form. */
    fun resetMfaState() {
        _uiState.update {
            it.copy(requiresMfa = false, mfaMethod = null, mfaEmail = null)
        }
    }

    /** Called when user dismisses the MFA setup required message — returns to login form. */
    fun dismissMfaSetup() {
        _uiState.update { it.copy(mfaSetupRequired = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

