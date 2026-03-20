package com.touchstoneinstitute.learningcompanion.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LearnerResultSummary
import com.touchstoneinstitute.learningcompanion.data.repository.AuthResult
import com.touchstoneinstitute.learningcompanion.data.repository.ResultsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val results: List<LearnerResultSummary> = emptyList(),
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val resultsRepository: ResultsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        loadAlerts()
    }

    fun loadAlerts() {
        viewModelScope.launch {
            _uiState.update { current ->
                val hasContent = current.results.isNotEmpty()
                current.copy(
                    isLoading = !hasContent,
                    isRefreshing = hasContent,
                    errorMessage = null,
                )
            }
            when (val result = resultsRepository.getReleasedResults()) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            results = result.data.results,
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message,
                        )
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false)
                    }
                }
            }
        }
    }
}

