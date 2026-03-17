package com.touchstoneinstitute.learningcompanion.data.remote

import com.touchstoneinstitute.learningcompanion.data.local.TokenManager
import com.touchstoneinstitute.learningcompanion.data.remote.dto.RefreshTokenRequest
import com.touchstoneinstitute.learningcompanion.data.remote.dto.RefreshTokenResponse
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Authenticator that automatically refreshes the access token on 401 responses.
 * If refresh fails, clears stored tokens so the user is returned to the login screen.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager
) : Authenticator {

    // Lazy-built Retrofit instance to avoid circular dependency with the main OkHttpClient.
    private val authApi by lazy {
        val client = OkHttpClient.Builder().build()
        Retrofit.Builder()
            .baseUrl(com.touchstoneinstitute.learningcompanion.BuildConfig.AUTH_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(com.touchstoneinstitute.learningcompanion.data.remote.api.AuthApi::class.java)
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Don't retry if we already attempted a refresh for this request chain
        if (responseCount(response) >= 2) {
            Timber.w("Token refresh retry limit reached — logging out")
            runBlocking { tokenManager.clearTokens() }
            return null
        }

        val refreshToken = runBlocking { tokenManager.getRefreshTokenSync() }
        if (refreshToken == null) {
            Timber.w("No refresh token available — logging out")
            runBlocking { tokenManager.clearTokens() }
            return null
        }

        return try {
            val refreshResponse: RefreshTokenResponse = runBlocking {
                authApi.refreshToken(RefreshTokenRequest(refreshToken))
            }
            val newAccessToken = refreshResponse.accessToken

            // Persist the new access token
            runBlocking { tokenManager.saveAccessToken(newAccessToken) }

            Timber.d("Token refreshed successfully")

            // Retry the original request with the new token
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } catch (e: Exception) {
            Timber.e(e, "Token refresh failed — logging out")
            runBlocking { tokenManager.clearTokens() }
            null
        }
    }

    /** Count how many times we've already tried to authenticate this request chain. */
    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}

