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
    val isCheckingSession: Boolean = true
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
                    is AuthResult.Error -> {
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
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

