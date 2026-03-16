package com.touchstoneinstitute.learningcompanion.data.repository

import com.touchstoneinstitute.learningcompanion.data.local.dao.ScheduleDayDao
import com.touchstoneinstitute.learningcompanion.data.local.entity.CachedScheduleDay
import com.touchstoneinstitute.learningcompanion.data.remote.api.MobileApi
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleDay
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.SessionSummary
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val mobileApi: MobileApi,
    private val scheduleDayDao: ScheduleDayDao
) {

    suspend fun getSchedule(): AuthResult<ScheduleResponse> {
        return try {
            val schedule = mobileApi.getSchedule()
            cacheSchedule(schedule)
            AuthResult.Success(schedule)
        } catch (e: retrofit2.HttpException) {
            tryFallbackFromCache("Failed to load schedule (${e.code()})", e)
        } catch (e: java.io.IOException) {
            tryFallbackFromCache("Network error. Please check your connection.", e)
        } catch (e: Exception) {
            tryFallbackFromCache("Could not load schedule", e)
        }
    }

    private suspend fun tryFallbackFromCache(message: String, cause: Throwable): AuthResult<ScheduleResponse> {
        return try {
            val cached = scheduleDayDao.getScheduleDays("default")
            if (cached.isNotEmpty()) {
                Timber.d("Returning ${cached.size} cached schedule rows")
                val response = cached.toScheduleResponse()
                AuthResult.Success(response)
            } else {
                AuthResult.Error(message, cause)
            }
        } catch (_: Exception) {
            AuthResult.Error(message, cause)
        }
    }

    private suspend fun cacheSchedule(response: ScheduleResponse) {
        try {
            val entities = response.days.flatMap { day ->
                day.sessions.map { session ->
                    CachedScheduleDay(
                        userId = "default",
                        date = day.date,
                        dayName = day.dayName,
                        sessionId = session.id,
                        sessionTitle = session.title,
                        sessionDate = session.date,
                        sessionStartTime = session.startTime,
                        sessionEndTime = session.endTime,
                        sessionLocation = session.location,
                        sessionType = session.type,
                        weekStart = response.weekStart,
                        weekEnd = response.weekEnd,
                        lastUpdated = response.lastUpdated
                    )
                }
            }
            scheduleDayDao.replaceForUser("default", entities)
        } catch (e: Exception) {
            Timber.w(e, "Failed to cache schedule")
        }
    }

    private fun List<CachedScheduleDay>.toScheduleResponse(): ScheduleResponse {
        val weekStart = firstOrNull()?.weekStart
        val weekEnd = firstOrNull()?.weekEnd
        val lastUpdated = firstOrNull()?.lastUpdated

        val days = groupBy { it.date to it.dayName }.map { (key, rows) ->
            ScheduleDay(
                date = key.first,
                dayName = key.second,
                sessions = rows.map { row ->
                    SessionSummary(
                        id = row.sessionId,
                        title = row.sessionTitle,
                        date = row.sessionDate,
                        startTime = row.sessionStartTime,
                        endTime = row.sessionEndTime,
                        location = row.sessionLocation,
                        type = row.sessionType
                    )
                }
            )
        }

        return ScheduleResponse(
            weekStart = weekStart,
            weekEnd = weekEnd,
            days = days,
            lastUpdated = lastUpdated
        )
    }
}

