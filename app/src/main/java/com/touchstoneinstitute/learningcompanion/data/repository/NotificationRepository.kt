package com.touchstoneinstitute.learningcompanion.data.repository

import android.os.Build
import com.touchstoneinstitute.learningcompanion.data.remote.api.MobileApi
import com.touchstoneinstitute.learningcompanion.data.remote.dto.DeviceRegistrationRequest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val mobileApi: MobileApi
) {

    /**
     * Register the FCM device token with the backend.
     * Called after login and on token refresh.
     */
    suspend fun registerDeviceToken(token: String): AuthResult<Unit> {
        return try {
            val request = DeviceRegistrationRequest(
                token = token,
                platform = "android",
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                osVersion = "Android ${Build.VERSION.RELEASE}"
            )
            mobileApi.registerDevice(request)
            Timber.d("Device token registered successfully")
            AuthResult.Success(Unit)
        } catch (e: retrofit2.HttpException) {
            Timber.w(e, "Failed to register device token (${e.code()})")
            AuthResult.Error("Failed to register device (${e.code()})", e)
        } catch (e: java.io.IOException) {
            Timber.w(e, "Network error registering device token")
            AuthResult.Error("Network error", e)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error registering device token")
            AuthResult.Error("Could not register device", e)
        }
    }
}

