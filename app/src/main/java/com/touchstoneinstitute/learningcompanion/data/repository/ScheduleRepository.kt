package com.touchstoneinstitute.learningcompanion.data.repository

import com.touchstoneinstitute.learningcompanion.data.local.dao.ScheduleDayDao
import com.touchstoneinstitute.learningcompanion.data.local.entity.CachedScheduleDay
import com.touchstoneinstitute.learningcompanion.data.remote.api.MobileApi
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleEntry
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleDay
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleWeek
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class ScheduleData(
    val response: ScheduleResponse,
    val isCached: Boolean = false,
)

@Singleton
class ScheduleRepository @Inject constructor(
    private val mobileApi: MobileApi,
    private val scheduleDayDao: ScheduleDayDao
) {

    suspend fun getSchedule(): AuthResult<ScheduleData> {
        return try {
            val schedule = mobileApi.getSchedule()
            cacheSchedule(schedule)
            AuthResult.Success(ScheduleData(response = schedule))
        } catch (e: retrofit2.HttpException) {
            val message = when (e.code()) {
                401 -> "Your session has expired. Please sign out and sign in again."
                403 -> "You don't have permission to view the schedule."
                404 -> "Your schedule hasn't been published yet. Check back soon."
                500, 502, 503 -> "The server encountered an error. Please try again later."
                else -> "Something went wrong loading your schedule."
            }
            tryFallbackFromCache(message, e)
        } catch (e: java.io.IOException) {
            tryFallbackFromCache("Network error. Please check your connection.", e)
        } catch (e: Exception) {
            tryFallbackFromCache("Could not load schedule", e)
        }
    }

    private suspend fun tryFallbackFromCache(message: String, cause: Throwable): AuthResult<ScheduleData> {
        return try {
            val cached = scheduleDayDao.getScheduleDays("default")
            if (cached.isNotEmpty()) {
                Timber.d("Returning ${cached.size} cached schedule rows")
                val response = cached.toScheduleResponse()
                AuthResult.Success(ScheduleData(response = response, isCached = true))
            } else {
                AuthResult.Error(message, cause)
            }
        } catch (_: Exception) {
            AuthResult.Error(message, cause)
        }
    }

    private suspend fun cacheSchedule(response: ScheduleResponse) {
        try {
            val entities = response.weeks.flatMap { week ->
                week.days.map { day ->
                    CachedScheduleDay(
                        userId = "default",
                        weekName = week.weekName,
                        day = day.day,
                        period = day.period,
                        sessionName = day.session.sessionName,
                        track = day.session.track,
                        group = day.session.group,
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
        val lastUpdated = firstOrNull()?.lastUpdated
        val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val periodOrder = listOf("AM", "PM")

        val weeks = groupBy { it.weekName }
            .entries
            .map { (weekName, rows) ->
                ScheduleWeek(
                    weekName = weekName,
                    days = rows
                        .map { row ->
                            ScheduleDay(
                                day = row.day,
                                period = row.period,
                                session = ScheduleEntry(
                                    sessionName = row.sessionName,
                                    track = row.track,
                                    group = row.group,
                                )
                            )
                        }
                        .sortedWith(
                            compareBy<ScheduleDay>(
                                { dayOrder.indexOf(it.day).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE },
                                { periodOrder.indexOf(it.period).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE },
                            )
                        )
                )
            }

        return ScheduleResponse(
            lastUpdated = lastUpdated,
            weeks = weeks,
        )
    }
}

