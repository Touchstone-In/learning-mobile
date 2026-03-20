package com.touchstoneinstitute.learningcompanion.data.repository

import com.touchstoneinstitute.learningcompanion.data.local.dao.OverviewDao
import com.touchstoneinstitute.learningcompanion.data.local.entity.CachedOverview
import com.touchstoneinstitute.learningcompanion.data.remote.api.MobileApi
import com.touchstoneinstitute.learningcompanion.data.remote.api.UserApi
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LearnerOverviewResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.UserDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class HomeRepositoryTest {

    private lateinit var userApi: UserApi
    private lateinit var mobileApi: MobileApi
    private lateinit var overviewDao: OverviewDao
    private lateinit var repository: HomeRepository

    @Before
    fun setUp() {
        userApi = mockk()
        mobileApi = mockk()
        overviewDao = mockk(relaxUnitFun = true)
        repository = HomeRepository(userApi, mobileApi, overviewDao)
    }

    @Test
    fun `getHomeData returns user and overview on success`() = runTest {
        val user = UserDto(id = "1", email = "t@tsin.ca", firstName = "Jane")
        val overview = LearnerOverviewResponse(programName = "PRP", programType = "PRP", applicationStatus = "Active", registrationStatus = "Registered")
        coEvery { userApi.getMe() } returns user
        coEvery { mobileApi.getOverview() } returns overview

        val result = repository.getHomeData()

        assertTrue(result is AuthResult.Success)
        val data = (result as AuthResult.Success).data
        assertEquals("Jane", data.user?.firstName)
        assertEquals("PRP", data.overview?.programName)
        assertFalse(data.isCached)
        coVerify { overviewDao.insertOverview(any()) }
    }

    @Test
    fun `getHomeData returns user with null overview when mobile api fails`() = runTest {
        val user = UserDto(id = "1", email = "t@tsin.ca")
        coEvery { userApi.getMe() } returns user
        coEvery { mobileApi.getOverview() } throws HttpException(
            Response.error<Any>(404, "".toResponseBody())
        )

        val result = repository.getHomeData()

        assertTrue(result is AuthResult.Success)
        val data = (result as AuthResult.Success).data
        assertNotNull(data.user)
        assertNull(data.overview)
    }

    @Test
    fun `getHomeData returns error when user api fails and no cache`() = runTest {
        coEvery { userApi.getMe() } throws IOException("offline")
        coEvery { overviewDao.getOverview("default") } returns null

        val result = repository.getHomeData()

        assertTrue(result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("Network error"))
    }

    @Test
    fun `getHomeData falls back to cache when user api fails`() = runTest {
        coEvery { userApi.getMe() } throws IOException("offline")
        coEvery { overviewDao.getOverview("default") } returns CachedOverview(
            userId = "default",
            programName = "Cached Program",
            programType = "PRP",
            applicationStatus = "Active",
            registrationStatus = "Registered",
            nextSessionName = null,
            nextSessionDay = null,
            nextSessionPeriod = null,
            nextSessionTrack = null,
            nextSessionGroup = null,
            keyDatesJson = "[]",
            cachedAt = System.currentTimeMillis()
        )

        val result = repository.getHomeData()

        assertTrue(result is AuthResult.Success)
        val data = (result as AuthResult.Success).data
        assertTrue(data.isCached)
        assertNull(data.user)
        assertEquals("Cached Program", data.overview?.programName)
    }
}

