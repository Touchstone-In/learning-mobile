package com.touchstoneinstitute.learningcompanion.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Response from the auth service (userBaseUrl/auth/authenticate-profiile).
 * When MFA is required, token will be null and requiresMfa will be true.
 * When MFA is not required, token will be present.
 */
data class LoginResponse(
    val success: Boolean? = null,
    val token: String? = null,
    val refreshToken: String? = null,
    val id: String? = null,
    val email: String? = null,
    val role: String? = null,
    /** Auth service user object — includes isOtpEnabled / otpMeans. */
    val user: AuthUserDto? = null,
    val requiresMfa: Boolean? = null,
    val mfaMethod: String? = null,
    val skipValidation: Boolean? = null
)

/** User object returned by the auth service in login / OTP responses. */
data class AuthUserDto(
    val id: String? = null,
    val email: String? = null,
    val profiles: List<ProfileDto>? = null,
    val isOtpEnabled: Boolean? = null,
    val otpMeans: String? = null
)

// ── Token Refresh ───────────────────────────────────────────────────

data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val accessToken: String
)

// ── MFA / OTP Verification ──────────────────────────────────────────

data class VerifyOtpRequest(
    val email: String,
    val token: String
)

/**
 * Response returned by validate-otp / confirm-email-2fa.
 * Same shape as a successful LoginResult from the auth service.
 */
data class MfaVerificationResponse(
    val success: Boolean? = null,
    val token: String? = null,
    val refreshToken: String? = null,
    val expiresIn: Int? = null,
    val user: AuthUserDto? = null
)

data class ProfileDto(
    val id: String? = null,
    val role: String? = null,
    val project: String? = null
)

/**
 * User profile from the learning backend (/user/me).
 */
data class UserDto(
    @SerializedName("_id") val mongoId: String? = null,
    val id: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: String? = null,
    val avatar: String? = null,
    val learnerNumber: String? = null,
    val learnerType: String? = null,
    val status: String? = null,
    val accountStatus: String? = null
)

