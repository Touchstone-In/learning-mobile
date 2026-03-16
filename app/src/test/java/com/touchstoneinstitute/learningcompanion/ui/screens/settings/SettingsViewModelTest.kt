package com.touchstoneinstitute.learningcompanion.ui.screens.settings

import com.touchstoneinstitute.learningcompanion.data.remote.api.MobileApi
import com.touchstoneinstitute.learningcompanion.data.remote.api.UserApi
import com.touchstoneinstitute.learningcompanion.data.remote.dto.NotificationPreferencesResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.UpdatePreferencesRequest
import com.touchstoneinstitute.learningcompanion.data.remote.dto.UserDto
import com.touchstoneinstitute.learningcompanion.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userApi: UserApi
    private lateinit var mobileApi: MobileApi
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userApi = mockk()
        mobileApi = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)

        // Default preference response
        coEvery { mobileApi.getPreferences() } returns NotificationPreferencesResponse()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads user profile on init`() = runTest {
        val user = UserDto(
            id = "u1", email = "jane@tsin.ca",
            firstName = "Jane", lastName = "Doe", role = "learner"
        )
        coEvery { userApi.getMe() } returns user

        val vm = SettingsViewModel(userApi, mobileApi, authRepository)
        advanceUntilIdle()

        assertEquals("Jane", vm.uiState.value.firstName)
        assertEquals("Doe", vm.uiState.value.lastName)
        assertEquals("jane@tsin.ca", vm.uiState.value.email)
        assertEquals("learner", vm.uiState.value.role)
    }

    @Test
    fun `profile load failure keeps defaults`() = runTest {
        coEvery { userApi.getMe() } throws RuntimeException("Server error")

        val vm = SettingsViewModel(userApi, mobileApi, authRepository)
        advanceUntilIdle()

        assertNull(vm.uiState.value.firstName)
        assertNull(vm.uiState.value.email)
    }

    @Test
    fun `logout delegates to auth repository`() = runTest {
        coEvery { userApi.getMe() } returns UserDto(id = "u1")

        val vm = SettingsViewModel(userApi, mobileApi, authRepository)
        advanceUntilIdle()

        vm.logout()

        coVerify { authRepository.logout() }
    }

    @Test
    fun `loads notification preferences on init`() = runTest {
        coEvery { userApi.getMe() } returns UserDto(id = "u1")
        coEvery { mobileApi.getPreferences() } returns NotificationPreferencesResponse(
            pushEnabled = false,
            scheduleReminders = true,
            orientationReminders = false
        )

        val vm = SettingsViewModel(userApi, mobileApi, authRepository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.pushEnabled)
        assertTrue(vm.uiState.value.scheduleReminders)
        assertFalse(vm.uiState.value.orientationReminders)
    }

    @Test
    fun `toggle push enabled updates state and calls API`() = runTest {
        coEvery { userApi.getMe() } returns UserDto(id = "u1")
        coEvery { mobileApi.updatePreferences(any()) } returns NotificationPreferencesResponse(
            pushEnabled = false,
            scheduleReminders = true,
            orientationReminders = true
        )

        val vm = SettingsViewModel(userApi, mobileApi, authRepository)
        advanceUntilIdle()

        vm.togglePushEnabled(false)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.pushEnabled)
        coVerify {
            mobileApi.updatePreferences(match { it.pushEnabled == false })
        }
    }

    @Test
    fun `toggle schedule reminders updates state and calls API`() = runTest {
        coEvery { userApi.getMe() } returns UserDto(id = "u1")
        coEvery { mobileApi.updatePreferences(any()) } returns NotificationPreferencesResponse(
            pushEnabled = true,
            scheduleReminders = false,
            orientationReminders = true
        )

        val vm = SettingsViewModel(userApi, mobileApi, authRepository)
        advanceUntilIdle()

        vm.toggleScheduleReminders(false)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.scheduleReminders)
        coVerify {
            mobileApi.updatePreferences(match { it.scheduleReminders == false })
        }
    }
}