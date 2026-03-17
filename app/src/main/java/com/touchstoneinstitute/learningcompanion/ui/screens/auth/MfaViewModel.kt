package com.touchstoneinstitute.learningcompanion.ui.screens.auth

import androidx.lifecycle.SavedStateHandle
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

data class MfaUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isVerified: Boolean = false,
    val mfaMethod: String = "App",
    val email: String = ""
)

@HiltViewModel
class MfaViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MfaUiState(
            email = savedStateHandle.get<String>("email") ?: "",
            mfaMethod = savedStateHandle.get<String>("mfaMethod") ?: "App"
        )
    )
    val uiState: StateFlow<MfaUiState> = _uiState.asStateFlow()

    fun verifyCode(code: String) {
        val trimmedCode = code.trim()
        if (trimmedCode.length != 6) {
            _uiState.update { it.copy(errorMessage = "Please enter a 6-digit code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = when (_uiState.value.mfaMethod) {
                "Email" -> authRepository.verifyEmailOtp(trimmedCode)
                else -> authRepository.verifyAppOtp(_uiState.value.email, trimmedCode)
            }

            when (result) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isVerified = true) }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is AuthResult.MfaSetupRequired -> {
                    // Should not happen during OTP verification
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Unexpected error")
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

