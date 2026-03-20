package com.touchstoneinstitute.learningcompanion.data.remote.api

import com.touchstoneinstitute.learningcompanion.data.remote.dto.DeviceRegistrationRequest
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LearnerOverviewResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LearnerResultsResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.NotificationPreferencesResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.UpdatePreferencesRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface MobileApi {

    @GET("mobile/me/overview")
    suspend fun getOverview(): LearnerOverviewResponse

    @GET("mobile/me/schedule")
    suspend fun getSchedule(): ScheduleResponse

    @GET("mobile/me/results")
    suspend fun getResults(): LearnerResultsResponse

    @POST("mobile/devices")
    suspend fun registerDevice(@Body request: DeviceRegistrationRequest)

    @GET("mobile/preferences")
    suspend fun getPreferences(): NotificationPreferencesResponse

    @PATCH("mobile/preferences")
    suspend fun updatePreferences(@Body request: UpdatePreferencesRequest): NotificationPreferencesResponse
}