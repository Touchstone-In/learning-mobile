package com.touchstoneinstitute.learningcompanion.data.remote.api

import com.touchstoneinstitute.learningcompanion.data.remote.dto.LoginRequest
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LoginResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.MfaVerificationResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.RefreshTokenRequest
import com.touchstoneinstitute.learningcompanion.data.remote.dto.RefreshTokenResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.VerifyOtpRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Auth API — calls the external auth service (userBaseUrl).
 */
interface AuthApi {

    @POST("auth/authenticate-profiile")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshTokenResponse

    /** Verify App-based TOTP code */
    @POST("auth/validate-otp")
    suspend fun validateOtp(@Body request: VerifyOtpRequest): MfaVerificationResponse

    /** Verify Email-based OTP code */
    @GET("auth/confirm-email-2fa/{otp}")
    suspend fun confirmEmailOtp(@Path("otp") otp: String): MfaVerificationResponse
}

