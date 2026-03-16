package com.touchstoneinstitute.learningcompanion.data.repository

import com.touchstoneinstitute.learningcompanion.data.remote.api.MobileApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class NotificationRepositoryTest {

    private lateinit var mobileApi: MobileApi
    private lateinit var repository: NotificationRepository

    @Before
    fun setUp() {
        mobileApi = mockk()
        repository = NotificationRepository(mobileApi)
    }

    @Test
    fun `registerDeviceToken success returns Success`() = runTest {
        coEvery { mobileApi.registerDevice(any()) } returns Unit

        val result = repository.registerDeviceToken("fcm-token-123")

        assertTrue(result is AuthResult.Success)
        coVerify { mobileApi.registerDevice(match { it.token == "fcm-token-123" && it.platform == "android" }) }
    }

    @Test
    fun `registerDeviceToken http error returns Error`() = runTest {
        coEvery { mobileApi.registerDevice(any()) } throws HttpException(
            Response.error<Any>(500, "".toResponseBody())
        )

        val result = repository.registerDeviceToken("fcm-token-123")

        assertTrue(result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("500"))
    }

    @Test
    fun `registerDeviceToken network error returns Error`() = runTest {
        coEvery { mobileApi.registerDevice(any()) } throws IOException("offline")

        val result = repository.registerDeviceToken("fcm-token-123")

        assertTrue(result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("Network error"))
    }
}

