package com.touchstoneinstitute.learningcompanion.data.repository

import com.touchstoneinstitute.learningcompanion.data.local.TokenManager
import com.touchstoneinstitute.learningcompanion.data.remote.api.AuthApi
import com.touchstoneinstitute.learningcompanion.data.remote.api.UserApi
import com.touchstoneinstitute.learningcompanion.data.remote.dto.AuthUserDto
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LoginRequest
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LoginResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.UserDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AuthRepositoryTest {

    private lateinit var authApi: AuthApi
    private lateinit var userApi: UserApi
    private lateinit var tokenManager: TokenManager
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        authApi = mockk()
        userApi = mockk()
        tokenManager = mockk(relaxed = true)
        repository = AuthRepository(authApi, userApi, tokenManager)
    }

    // ---- login() ----

    @Test
    fun `login success stores tokens and returns Success`() = runTest {
        val response = LoginResponse(
            token = "jwt-token-123",
            refreshToken = "refresh-456",
            id = "user-1",
            email = "test@tsin.ca",
            user = AuthUserDto(
                id = "user-1",
                email = "test@tsin.ca",
                isOtpEnabled = true,
                otpMeans = "App"
            )
        )
        coEvery { authApi.login(any()) } returns response

        val result = repository.login("test@tsin.ca", "pass123")

        assertTrue(result is AuthResult.Success)
        assertEquals("jwt-token-123", (result as AuthResult.Success).data.token)
        coVerify { tokenManager.saveTokens("jwt-token-123", "refresh-456") }
    }

    @Test
    fun `login without MFA setup returns setup required`() = runTest {
        val response = LoginResponse(
            token = "jwt-token-123",
            refreshToken = "refresh-456",
            id = "user-1",
            email = "test@tsin.ca",
            user = AuthUserDto(
                id = "user-1",
                email = "test@tsin.ca",
                isOtpEnabled = false,
                otpMeans = "None"
            )
        )
        coEvery { authApi.login(any()) } returns response

        val result = repository.login("test@tsin.ca", "pass123")

        assertTrue(result is AuthResult.MfaSetupRequired)
    }

    @Test
    fun `login requiring MFA returns success without storing tokens`() = runTest {
        val response = LoginResponse(
            email = "test@tsin.ca",
            requiresMfa = true,
            mfaMethod = "App",
            user = AuthUserDto(
                id = "user-1",
                email = "test@tsin.ca",
                isOtpEnabled = true,
                otpMeans = "App"
            )
        )
        coEvery { authApi.login(any()) } returns response

        val result = repository.login("test@tsin.ca", "pass123")

        assertTrue(result is AuthResult.Success)
        assertTrue((result as AuthResult.Success).data.requiresMfa == true)
        coVerify(exactly = 0) { tokenManager.saveTokens(any(), any()) }
    }

    @Test
    fun `login 401 returns invalid credentials error`() = runTest {
        coEvery { authApi.login(any()) } throws HttpException(
            Response.error<Any>(401, "".toResponseBody())
        )

        val result = repository.login("bad@tsin.ca", "wrong")

        assertTrue(result is AuthResult.Error)
        assertEquals("Invalid email or password", (result as AuthResult.Error).message)
    }

    @Test
    fun `login 403 returns account locked error`() = runTest {
        coEvery { authApi.login(any()) } throws HttpException(
            Response.error<Any>(403, "".toResponseBody())
        )

        val result = repository.login("locked@tsin.ca", "pass")

        assertTrue(result is AuthResult.Error)
        assertEquals("Account is locked or disabled", (result as AuthResult.Error).message)
    }

    @Test
    fun `login IOException returns network error`() = runTest {
        coEvery { authApi.login(any()) } throws IOException("No network")

        val result = repository.login("test@tsin.ca", "pass")

        assertTrue(result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("Network error"))
    }

    // ---- validateSession() ----

    @Test
    fun `validateSession success returns user`() = runTest {
        val user = UserDto(id = "u1", email = "test@tsin.ca", firstName = "John")
        coEvery { userApi.getMe() } returns user

        val result = repository.validateSession()

        assertTrue(result is AuthResult.Success)
        assertEquals("John", (result as AuthResult.Success).data.firstName)
    }

    @Test
    fun `validateSession 401 clears tokens`() = runTest {
        coEvery { userApi.getMe() } throws HttpException(
            Response.error<Any>(401, "".toResponseBody())
        )

        val result = repository.validateSession()

        assertTrue(result is AuthResult.Error)
        coVerify { tokenManager.clearTokens() }
    }

    // ---- logout() ----

    @Test
    fun `logout clears tokens`() = runTest {
        repository.logout()
        coVerify { tokenManager.clearTokens() }
    }

    // ---- hasStoredToken() ----

    @Test
    fun `hasStoredToken delegates to tokenManager`() = runTest {
        coEvery { tokenManager.isLoggedIn() } returns true
        assertTrue(repository.hasStoredToken())

        coEvery { tokenManager.isLoggedIn() } returns false
        assertTrue(!repository.hasStoredToken())
    }
}

