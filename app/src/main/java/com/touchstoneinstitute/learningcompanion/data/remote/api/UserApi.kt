package com.touchstoneinstitute.learningcompanion.data.remote.api

import com.touchstoneinstitute.learningcompanion.data.remote.dto.UserDto
import retrofit2.http.GET

/**
 * User API — calls the learning backend for user profile operations.
 */
interface UserApi {

    @GET("user/me")
    suspend fun getMe(): UserDto
}

