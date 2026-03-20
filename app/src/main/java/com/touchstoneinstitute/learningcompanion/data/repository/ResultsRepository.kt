package com.touchstoneinstitute.learningcompanion.data.repository

import com.touchstoneinstitute.learningcompanion.data.remote.api.MobileApi
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LearnerResultsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResultsRepository @Inject constructor(
    private val mobileApi: MobileApi
) {
    suspend fun getReleasedResults(): AuthResult<LearnerResultsResponse> {
        return try {
            val response = mobileApi.getResults()
            AuthResult.Success(response)
        } catch (e: retrofit2.HttpException) {
            AuthResult.Error("Failed to load results (${e.code()})", e)
        } catch (e: java.io.IOException) {
            AuthResult.Error("Network error. Please check your connection.", e)
        } catch (e: Exception) {
            AuthResult.Error("Could not load results", e)
        }
    }
}

