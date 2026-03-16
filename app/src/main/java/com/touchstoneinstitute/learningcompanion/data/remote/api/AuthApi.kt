package com.touchstoneinstitute.learningcompanion.data.remote.api

import com.touchstoneinstitute.learningcompanion.data.remote.dto.LoginRequest
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Auth API — calls the external auth service (userBaseUrl).
 */
interface AuthApi {

    @POST("auth/authenticate-profiile")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}

