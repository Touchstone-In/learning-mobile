package com.touchstoneinstitute.learningcompanion.ui.screens.schedule

import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleDay
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleEntry
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleWeek
import com.touchstoneinstitute.learningcompanion.data.repository.AuthResult
import com.touchstoneinstitute.learningcompanion.data.repository.ScheduleData
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
    fun `loads schedule on init with weeks and week label`() = runTest {
        val days = listOf(
            ScheduleDay(
                day = "Monday", period = "AM",
                session = ScheduleEntry(sessionName = "Clinical Skills", track = "A", group = "1")
            )
        )
        val weeks = listOf(ScheduleWeek(weekName = "Week 1", days = days))
        val response = ScheduleResponse(
            lastUpdated = "2026-03-16T08:00:00Z",
            weeks = weeks,
        )
        coEvery { scheduleRepository.getSchedule() } returns AuthResult.Success(
            ScheduleData(response = response)
        )

        val vm = ScheduleViewModel(scheduleRepository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(1, vm.uiState.value.weeks.size)
        assertEquals("Clinical Skills", vm.uiState.value.weeks[0].days[0].session.sessionName)
        assertEquals("Week 1", vm.uiState.value.weekLabel)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `shows error on failure`() = runTest {
        coEvery { scheduleRepository.getSchedule() } returns AuthResult.Error("Failed to load schedule (500)")

        val vm = ScheduleViewModel(scheduleRepository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals("Failed to load schedule (500)", vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.weeks.isEmpty())
    }

    @Test
    fun `empty schedule returns empty weeks list`() = runTest {
        coEvery { scheduleRepository.getSchedule() } returns AuthResult.Success(
            ScheduleData(response = ScheduleResponse(weeks = emptyList()))
        )

        val vm = ScheduleViewModel(scheduleRepository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.weeks.isEmpty())
        assertNull(vm.uiState.value.weekLabel)
    }

    @Test
    fun `refresh reloads schedule`() = runTest {
        coEvery { scheduleRepository.getSchedule() } returns AuthResult.Success(
            ScheduleData(response = ScheduleResponse(weeks = emptyList()))
        )

        val vm = ScheduleViewModel(scheduleRepository)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.weeks.isEmpty())

        val updatedWeeks = listOf(
            ScheduleWeek(
                weekName = "Week 2",
                days = listOf(
                    ScheduleDay(
                        day = "Tuesday", period = "PM",
                        session = ScheduleEntry(sessionName = "Lab", track = "B", group = "2")
                    )
                )
            )
        )
        coEvery { scheduleRepository.getSchedule() } returns AuthResult.Success(
            ScheduleData(response = ScheduleResponse(weeks = updatedWeeks))
        )

        vm.loadSchedule()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.weeks.size)
        assertEquals("Tuesday", vm.uiState.value.weeks[0].days[0].day)
    }

    @Test
    fun `cached schedule result exposes offline flag`() = runTest {
        val cachedWeeks = listOf(
            ScheduleWeek(
                weekName = "Week 3",
                days = listOf(
                    ScheduleDay(
                        day = "Wednesday", period = "AM",
                        session = ScheduleEntry(sessionName = "Orientation", track = "A", group = "1")
                    )
                )
            )
        )
        coEvery { scheduleRepository.getSchedule() } returns AuthResult.Success(
            ScheduleData(
                response = ScheduleResponse(weeks = cachedWeeks),
                isCached = true,
            )
        )

        val vm = ScheduleViewModel(scheduleRepository)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isCached)
        assertEquals("Orientation", vm.uiState.value.weeks.first().days.first().session.sessionName)
    }
}

