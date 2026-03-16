package com.touchstoneinstitute.learningcompanion.ui.screens.schedule

import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleDay
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.SessionSummary
import com.touchstoneinstitute.learningcompanion.data.repository.AuthResult
import com.touchstoneinstitute.learningcompanion.data.repository.ScheduleRepository
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
class ScheduleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var scheduleRepository: ScheduleRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        scheduleRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads schedule on init with days and week label`() = runTest {
        val sessions = listOf(
            SessionSummary(id = "s1", title = "Clinical Skills", date = "2026-03-16",
                startTime = "09:00", endTime = "12:00", location = "Room 204")
        )
        val days = listOf(ScheduleDay(date = "2026-03-16", dayName = "Monday", sessions = sessions))
        val response = ScheduleResponse(
            weekStart = "Mar 16", weekEnd = "Mar 20",
            days = days, lastUpdated = "2026-03-16T08:00:00Z"
        )
        coEvery { scheduleRepository.getSchedule() } returns AuthResult.Success(response)

        val vm = ScheduleViewModel(scheduleRepository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(1, vm.uiState.value.days.size)
        assertEquals("Clinical Skills", vm.uiState.value.days[0].sessions[0].title)
        assertEquals("Mar 16 — Mar 20", vm.uiState.value.weekLabel)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `shows error on failure`() = runTest {
        coEvery { scheduleRepository.getSchedule() } returns AuthResult.Error("Failed to load schedule (500)")

        val vm = ScheduleViewModel(scheduleRepository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals("Failed to load schedule (500)", vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.days.isEmpty())
    }

    @Test
    fun `empty schedule returns empty days list`() = runTest {
        coEvery { scheduleRepository.getSchedule() } returns AuthResult.Success(
            ScheduleResponse(days = emptyList())
        )

        val vm = ScheduleViewModel(scheduleRepository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.days.isEmpty())
        assertNull(vm.uiState.value.weekLabel)
    }

    @Test
    fun `refresh reloads schedule`() = runTest {
        coEvery { scheduleRepository.getSchedule() } returns AuthResult.Success(
            ScheduleResponse(days = emptyList())
        )

        val vm = ScheduleViewModel(scheduleRepository)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.days.isEmpty())

        val updatedDays = listOf(
            ScheduleDay(date = "2026-03-17", dayName = "Tuesday", sessions = emptyList())
        )
        coEvery { scheduleRepository.getSchedule() } returns AuthResult.Success(
            ScheduleResponse(days = updatedDays)
        )

        vm.loadSchedule()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.days.size)
        assertEquals("Tuesday", vm.uiState.value.days[0].dayName)
    }
}

