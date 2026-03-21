package com.touchstoneinstitute.learningcompanion.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchstoneinstitute.learningcompanion.data.remote.api.MobileApi
import com.touchstoneinstitute.learningcompanion.data.remote.api.UserApi
import com.touchstoneinstitute.learningcompanion.data.remote.dto.UpdatePreferencesRequest
import com.touchstoneinstitute.learningcompanion.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SettingsUiState(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val role: String? = null,
    val pushEnabled: Boolean = true,
    val scheduleReminders: Boolean = true,
    val orientationReminders: Boolean = true,
    val preferencesLoading: Boolean = false,
    val preferencesSaving: Boolean = false,
    val preferenceSuccessMessage: String? = null,
    val preferenceErrorMessage: String? = null,
    /** True when the error came from loading preferences (as opposed to saving them). */
    val isPreferenceLoadError: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userApi: UserApi,
    private val mobileApi: MobileApi,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadPreferences()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val user = userApi.getMe()
                _uiState.update {
                    it.copy(
                        firstName = user.firstName,
                        lastName = user.lastName,
                        email = user.email,
                        role = user.role
                    )
                }
            } catch (_: Exception) {
                // Silently handle  settings page will show defaults
            }
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(preferencesLoading = true) }
            try {
                val prefs = mobileApi.getPreferences()
                _uiState.update {
                    it.copy(
                        pushEnabled = prefs.pushEnabled,
                        scheduleReminders = prefs.scheduleReminders,
                        orientationReminders = prefs.orientationReminders,
                        preferencesLoading = false,
                    )
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to load notification preferences")
                _uiState.update {
                    it.copy(
                        preferencesLoading = false,
                        preferenceSuccessMessage = null,
                        preferenceErrorMessage = "We couldn't load your alert preferences right now.",
                        isPreferenceLoadError = true,
                    )
                }
            }
        }
    }


    private fun restorePreferencesFromServer() {
        viewModelScope.launch {
            _uiState.update { it.copy(preferencesLoading = true) }
            try {
                val prefs = mobileApi.getPreferences()
                _uiState.update {
                    it.copy(
                        pushEnabled = prefs.pushEnabled,
                        scheduleReminders = prefs.scheduleReminders,
                        orientationReminders = prefs.orientationReminders,
                        preferencesLoading = false,
                    )
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to restore notification preferences")
                _uiState.update { it.copy(preferencesLoading = false) }
            }
        }
    }

    fun togglePushEnabled(enabled: Boolean) {
        _uiState.update {
            it.copy(
                pushEnabled = enabled,
                preferenceSuccessMessage = null,
                preferenceErrorMessage = null,
                isPreferenceLoadError = false,
            )
        }
        updateRemotePreferences(UpdatePreferencesRequest(pushEnabled = enabled))
    }

    fun toggleScheduleReminders(enabled: Boolean) {
        _uiState.update {
            it.copy(
                scheduleReminders = enabled,
                preferenceSuccessMessage = null,
                preferenceErrorMessage = null,
                isPreferenceLoadError = false,
            )
        }
        updateRemotePreferences(UpdatePreferencesRequest(scheduleReminders = enabled))
    }

    fun toggleOrientationReminders(enabled: Boolean) {
        _uiState.update {
            it.copy(
                orientationReminders = enabled,
                preferenceSuccessMessage = null,
                preferenceErrorMessage = null,
                isPreferenceLoadError = false,
            )
        }
        updateRemotePreferences(UpdatePreferencesRequest(orientationReminders = enabled))
    }

    private fun updateRemotePreferences(request: UpdatePreferencesRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(preferencesSaving = true) }
            try {
                val updated = mobileApi.updatePreferences(request)
                _uiState.update {
                    it.copy(
                        pushEnabled = updated.pushEnabled,
                        scheduleReminders = updated.scheduleReminders,
                        orientationReminders = updated.orientationReminders,
                        preferencesSaving = false,
                        preferenceSuccessMessage = "Alert preferences updated.",
                        preferenceErrorMessage = null,
                    )
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to update notification preferences")
                _uiState.update {
                    it.copy(
                        preferencesSaving = false,
                        preferenceSuccessMessage = null,
                        preferenceErrorMessage = "Couldnâ€™t save alert preferences. Restoring your last saved settings.",
                    )
                }
                restorePreferencesFromServer()
            }
        }
    }

    suspend fun logout() {
        authRepository.logout()
    }
}
