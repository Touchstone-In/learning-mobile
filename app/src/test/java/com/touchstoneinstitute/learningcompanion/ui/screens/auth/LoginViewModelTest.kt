package com.touchstoneinstitute.learningcompanion.ui.screens.auth

import com.touchstoneinstitute.learningcompanion.data.remote.dto.LoginResponse
import com.touchstoneinstitute.learningcompanion.data.repository.AuthRepository
import com.touchstoneinstitute.learningcompanion.data.repository.AuthResult
import io.mockk.coEvery
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
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        // Default: no stored token (fresh app launch)
        coEvery { authRepository.hasStoredToken() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): LoginViewModel {
        return LoginViewModel(authRepository)
    }

    @Test
    fun `initial state shows checking session then completes`() = runTest {
        viewModel = createViewModel()
        // Before advancing coroutines, session check is pending
        assertTrue(viewModel.uiState.value.isCheckingSession)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isCheckingSession)
        assertFalse(viewModel.uiState.value.isLoggedIn)
    }

    @Test
    fun `login success sets isLoggedIn true`() = runTest {
        coEvery { authRepository.login("user@tsin.ca", "pass") } returns AuthResult.Success(
            LoginResponse(token = "tok", refreshToken = null)
        )
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("user@tsin.ca", "pass")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoggedIn)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `login failure sets error message`() = runTest {
        coEvery { authRepository.login("bad@tsin.ca", "wrong") } returns
                AuthResult.Error("Invalid email or password")
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("bad@tsin.ca", "wrong")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Invalid email or password", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `existing valid session auto-logs in`() = runTest {
        coEvery { authRepository.hasStoredToken() } returns true
        coEvery { authRepository.validateSession() } returns AuthResult.Success(mockk())

        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoggedIn)
        assertFalse(viewModel.uiState.value.isCheckingSession)
    }

    @Test
    fun `existing expired session shows login form`() = runTest {
        coEvery { authRepository.hasStoredToken() } returns true
        coEvery { authRepository.validateSession() } returns AuthResult.Error("Session expired")

        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertFalse(viewModel.uiState.value.isCheckingSession)
    }

    @Test
    fun `clearError removes error message`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns
                AuthResult.Error("Some error")
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("a@b.c", "x")
        advanceUntilIdle()
        assertEquals("Some error", viewModel.uiState.value.errorMessage)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }
}

