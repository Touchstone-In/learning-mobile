package com.touchstoneinstitute.learningcompanion.data.repository

import com.touchstoneinstitute.learningcompanion.data.local.TokenManager
import com.touchstoneinstitute.learningcompanion.data.remote.api.AuthApi
import com.touchstoneinstitute.learningcompanion.data.remote.api.UserApi
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LoginRequest
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LoginResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.UserDto
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : AuthResult<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val tokenManager: TokenManager
) {

    /**
     * Login with email and password via the external auth service.
     * On success, stores tokens and returns the login response.
     */
    suspend fun login(email: String, password: String): AuthResult<LoginResponse> {
        return try {
            val response = authApi.login(LoginRequest(email = email, password = password))
            tokenManager.saveTokens(
                accessToken = response.token,
                refreshToken = response.refreshToken
            )
            AuthResult.Success(response)
        } catch (e: retrofit2.HttpException) {
            val message = when (e.code()) {
                401 -> "Invalid email or password"
                403 -> "Account is locked or disabled"
                404 -> "Account not found"
                else -> "Login failed (${e.code()})"
            }
            AuthResult.Error(message, e)
        } catch (e: java.io.IOException) {
            AuthResult.Error("Network error. Please check your connection.", e)
        } catch (e: Exception) {
            AuthResult.Error("An unexpected error occurred", e)
        }
    }

    /**
     * Validate the stored token by calling /user/me on the learning backend.
     * Returns the user profile if the token is valid.
     */
    suspend fun validateSession(): AuthResult<UserDto> {
        return try {
            val user = userApi.getMe()
            AuthResult.Success(user)
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 401) {
                tokenManager.clearTokens()
            }
            AuthResult.Error("Session expired", e)
        } catch (e: Exception) {
            AuthResult.Error("Could not validate session", e)
        }
    }

    /**
     * Log out by clearing stored tokens.
     */
    suspend fun logout() {
        tokenManager.clearTokens()
    }

    /**
     * Check if a token exists (does not validate it).
     */
    suspend fun hasStoredToken(): Boolean {
        return tokenManager.isLoggedIn()
    }
}

