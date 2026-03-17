package com.touchstoneinstitute.learningcompanion.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LearnerOverviewResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.UserDto
import com.touchstoneinstitute.learningcompanion.data.repository.AuthResult
import com.touchstoneinstitute.learningcompanion.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val user: UserDto? = null,
    val overview: LearnerOverviewResponse? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = homeRepository.getHomeData()) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = result.data.user,
                            overview = result.data.overview
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is AuthResult.MfaSetupRequired -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "MFA setup required")
                    }
                }
            }
        }
    }
}

