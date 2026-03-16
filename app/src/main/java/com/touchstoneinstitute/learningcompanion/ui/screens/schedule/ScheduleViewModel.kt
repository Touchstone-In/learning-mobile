package com.touchstoneinstitute.learningcompanion.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleDay
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
    val errorMessage: String? = null,
    val days: List<ScheduleDay> = emptyList(),
    val weekLabel: String? = null,
    val lastUpdated: String? = null
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
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = scheduleRepository.getSchedule()) {
                is AuthResult.Success -> {
                    val data = result.data
                    val weekLabel = if (data.weekStart != null && data.weekEnd != null) {
                        "${data.weekStart} — ${data.weekEnd}"
                    } else null
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            days = data.days,
                            weekLabel = weekLabel,
                            lastUpdated = data.lastUpdated
                        )
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
}

