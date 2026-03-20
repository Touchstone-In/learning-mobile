package com.touchstoneinstitute.learningcompanion.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleWeek
import com.touchstoneinstitute.learningcompanion.data.repository.AuthResult
import com.touchstoneinstitute.learningcompanion.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val weeks: List<ScheduleWeek> = emptyList(),
    val weekLabel: String? = null,
    val lastUpdated: String? = null,
    val isCached: Boolean = false,
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadSchedule()
    }

    fun loadSchedule() {
        viewModelScope.launch {
            _uiState.update { current ->
                val hasContent = current.weeks.isNotEmpty()
                current.copy(
                    isLoading = !hasContent,
                    isRefreshing = hasContent,
                    errorMessage = null,
                )
            }
            when (val result = scheduleRepository.getSchedule()) {
                is AuthResult.Success -> {
                    val data = result.data.response
                    val weekLabel = data.weeks.firstOrNull()?.weekName
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            weeks = data.weeks,
                            weekLabel = weekLabel,
                            lastUpdated = data.lastUpdated,
                            isCached = result.data.isCached,
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
                is AuthResult.MfaSetupRequired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = "MFA setup required",
                        )
                    }
                }
            }
        }
    }
}

