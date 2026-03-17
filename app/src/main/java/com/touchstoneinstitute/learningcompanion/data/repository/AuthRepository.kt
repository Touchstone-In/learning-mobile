package com.touchstoneinstitute.learningcompanion.data.repository

import com.touchstoneinstitute.learningcompanion.data.local.TokenManager
import com.touchstoneinstitute.learningcompanion.data.remote.api.AuthApi
import com.touchstoneinstitute.learningcompanion.data.remote.api.UserApi
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LoginRequest
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LoginResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.MfaVerificationResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.RefreshTokenRequest
import com.touchstoneinstitute.learningcompanion.data.remote.dto.UserDto
import com.touchstoneinstitute.learningcompanion.data.remote.dto.VerifyOtpRequest
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : AuthResult<Nothing>()
    /** Login succeeded but the user has not set up MFA in the portal. */
    object MfaSetupRequired : AuthResult<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val tokenManager: TokenManager
) {

    /**
     * Login with email and password via the external auth service.
     * If MFA is not required, stores tokens and returns the response.
     * If MFA is required, returns the response without tokens so the UI can
     * navigate to the MFA verification screen.
     */
    suspend fun login(email: String, password: String): AuthResult<LoginResponse> {
        return try {
            val response = authApi.login(LoginRequest(email = email, password = password))
            if (response.requiresMfa == true) {
                // MFA required — don't store tokens yet (there are none)
                AuthResult.Success(response)
            } else if (response.user?.isOtpEnabled != true) {
                // Login succeeded but MFA is not set up — block access
                AuthResult.MfaSetupRequired
            } else {
                // MFA is set up and verified — store tokens
                response.token?.let { token ->
                    tokenManager.saveTokens(
                        accessToken = token,
                        refreshToken = response.refreshToken
                    )
                }
                AuthResult.Success(response)
            }
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
     * Verify an App-based TOTP code after login indicated MFA is required.
     * On success, stores tokens.
     */
    suspend fun verifyAppOtp(email: String, code: String): AuthResult<MfaVerificationResponse> {
        return try {
            val response = authApi.validateOtp(VerifyOtpRequest(email = email, token = code))
            response.token?.let { token ->
                tokenManager.saveTokens(
                    accessToken = token,
                    refreshToken = response.refreshToken
                )
            }
            AuthResult.Success(response)
        } catch (e: retrofit2.HttpException) {
            val message = when (e.code()) {
                401 -> "Invalid verification code"
                400 -> "Invalid verification code"
                else -> "Verification failed (${e.code()})"
            }
            AuthResult.Error(message, e)
        } catch (e: java.io.IOException) {
            AuthResult.Error("Network error. Please check your connection.", e)
        } catch (e: Exception) {
            AuthResult.Error("An unexpected error occurred", e)
        }
    }

    /**
     * Verify an Email-based OTP code after login indicated MFA is required.
     * On success, stores tokens.
     */
    suspend fun verifyEmailOtp(otp: String): AuthResult<MfaVerificationResponse> {
        return try {
            val response = authApi.confirmEmailOtp(otp)
            response.token?.let { token ->
                tokenManager.saveTokens(
                    accessToken = token,
                    refreshToken = response.refreshToken
                )
            }
            AuthResult.Success(response)
        } catch (e: retrofit2.HttpException) {
            val message = when (e.code()) {
                401, 400 -> "Invalid verification code"
                404 -> "Invalid verification code"
                else -> "Verification failed (${e.code()})"
            }
            AuthResult.Error(message, e)
        } catch (e: java.io.IOException) {
            AuthResult.Error("Network error. Please check your connection.", e)
        } catch (e: Exception) {
            AuthResult.Error("An unexpected error occurred", e)
        }
    }

    /**
     * Manually refresh the access token using the stored refresh token.
     */
    suspend fun refreshAccessToken(): AuthResult<String> {
        return try {
            val refreshToken = tokenManager.getRefreshTokenSync()
                ?: return AuthResult.Error("No refresh token available")
            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
            tokenManager.saveAccessToken(response.accessToken)
            AuthResult.Success(response.accessToken)
        } catch (e: Exception) {
            tokenManager.clearTokens()
            AuthResult.Error("Session expired. Please log in again.", e)
        }
    }

    /**
     * Validate the stored token by calling /user/me on the learning backend.
     * On 401, attempts a token refresh before giving up.
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

