package com.touchstoneinstitute.learningcompanion.data.remote

import com.touchstoneinstitute.learningcompanion.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth header for auth service endpoints (login, refresh, OTP, etc.)
        val path = originalRequest.url.encodedPath
        if (path.contains("auth/")) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { tokenManager.getAccessTokenSync() }

        val request = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}

