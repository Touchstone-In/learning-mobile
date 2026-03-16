package com.touchstoneinstitute.learningcompanion.ui.screens.home

import com.touchstoneinstitute.learningcompanion.data.remote.dto.LearnerOverviewResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.UserDto
import com.touchstoneinstitute.learningcompanion.data.repository.AuthResult
import com.touchstoneinstitute.learningcompanion.data.repository.HomeData
import com.touchstoneinstitute.learningcompanion.data.repository.HomeRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var homeRepository: HomeRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        homeRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads home data on init`() = runTest {
        val user = UserDto(id = "1", firstName = "Alice")
        val overview = LearnerOverviewResponse(programName = "CMP")
        coEvery { homeRepository.getHomeData() } returns AuthResult.Success(
            HomeData(user = user, overview = overview)
        )

        val vm = HomeViewModel(homeRepository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals("Alice", vm.uiState.value.user?.firstName)
        assertEquals("CMP", vm.uiState.value.overview?.programName)
    }

    @Test
    fun `shows error on failure`() = runTest {
        coEvery { homeRepository.getHomeData() } returns AuthResult.Error("Network error")

        val vm = HomeViewModel(homeRepository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals("Network error", vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.user)
    }

    @Test
    fun `refresh reloads data`() = runTest {
        coEvery { homeRepository.getHomeData() } returns AuthResult.Success(
            HomeData(user = UserDto(id = "1", firstName = "Bob"), overview = null)
        )

        val vm = HomeViewModel(homeRepository)
        advanceUntilIdle()
        assertEquals("Bob", vm.uiState.value.user?.firstName)

        // Simulate updated data
        coEvery { homeRepository.getHomeData() } returns AuthResult.Success(
            HomeData(user = UserDto(id = "1", firstName = "Robert"), overview = null)
        )
        vm.loadHome()
        advanceUntilIdle()

        assertEquals("Robert", vm.uiState.value.user?.firstName)
    }
}

