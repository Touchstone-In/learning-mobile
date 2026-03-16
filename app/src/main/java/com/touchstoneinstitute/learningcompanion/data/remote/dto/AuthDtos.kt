package com.touchstoneinstitute.learningcompanion.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Response from the auth service (userBaseUrl/auth/authenticate-profiile).
 * The external auth service returns token, refreshToken, id, email, role, user.
 */
data class LoginResponse(
    val token: String,
    val refreshToken: String? = null,
    val id: String? = null,
    val email: String? = null,
    val role: String? = null,
    val user: UserDto? = null
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

