package com.touchstoneinstitute.learningcompanion.data.repository

import com.touchstoneinstitute.learningcompanion.data.local.dao.OverviewDao
import com.touchstoneinstitute.learningcompanion.data.local.entity.CachedOverview
import com.touchstoneinstitute.learningcompanion.data.remote.api.MobileApi
import com.touchstoneinstitute.learningcompanion.data.remote.api.UserApi
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LearnerOverviewResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.SessionSummary
import com.touchstoneinstitute.learningcompanion.data.remote.dto.UserDto
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class HomeData(
    val user: UserDto? = null,
    val overview: LearnerOverviewResponse? = null,
    val isCached: Boolean = false
)

@Singleton
class HomeRepository @Inject constructor(
    private val userApi: UserApi,
    private val mobileApi: MobileApi,
    private val overviewDao: OverviewDao
) {

    suspend fun getHomeData(): AuthResult<HomeData> {
        return try {
            val user = userApi.getMe()
            val overview = try {
                mobileApi.getOverview().also { cacheOverview(it) }
            } catch (_: Exception) {
                null
            }
            AuthResult.Success(HomeData(user = user, overview = overview))
        } catch (e: retrofit2.HttpException) {
            tryFallbackFromCache("Failed to load home data (${e.code()})", e)
        } catch (e: java.io.IOException) {
            tryFallbackFromCache("Network error. Please check your connection.", e)
        } catch (e: Exception) {
            tryFallbackFromCache("An unexpected error occurred", e)
        }
    }

    private suspend fun tryFallbackFromCache(message: String, cause: Throwable): AuthResult<HomeData> {
        return try {
            val cached = overviewDao.getOverview("default")
            if (cached != null) {
                Timber.d("Returning cached overview (cachedAt=${cached.cachedAt})")
                val overview = cached.toLearnerOverviewResponse()
                AuthResult.Success(HomeData(user = null, overview = overview, isCached = true))
            } else {
                AuthResult.Error(message, cause)
            }
        } catch (_: Exception) {
            AuthResult.Error(message, cause)
        }
    }

    private suspend fun cacheOverview(overview: LearnerOverviewResponse) {
        try {
            val entity = CachedOverview(
                userId = "default",
                programName = overview.programName,
                programStatus = overview.programStatus,
                completedSessions = overview.completedSessions,
                totalSessions = overview.totalSessions,
                nextSessionId = overview.nextSession?.id,
                nextSessionTitle = overview.nextSession?.title,
                nextSessionDate = overview.nextSession?.date,
                nextSessionStartTime = overview.nextSession?.startTime,
                nextSessionEndTime = overview.nextSession?.endTime,
                nextSessionLocation = overview.nextSession?.location,
                nextSessionType = overview.nextSession?.type,
                cachedAt = System.currentTimeMillis()
            )
            overviewDao.insertOverview(entity)
        } catch (e: Exception) {
            Timber.w(e, "Failed to cache overview")
        }
    }

    private fun CachedOverview.toLearnerOverviewResponse(): LearnerOverviewResponse {
        val session = if (nextSessionId != null && nextSessionTitle != null &&
            nextSessionDate != null && nextSessionStartTime != null && nextSessionEndTime != null
        ) {
            SessionSummary(
                id = nextSessionId,
                title = nextSessionTitle,
                date = nextSessionDate,
                startTime = nextSessionStartTime,
                endTime = nextSessionEndTime,
                location = nextSessionLocation,
                type = nextSessionType
            )
        } else null

        return LearnerOverviewResponse(
            programName = programName,
            programStatus = programStatus,
            completedSessions = completedSessions,
            totalSessions = totalSessions,
            nextSession = session
        )
    }
}

