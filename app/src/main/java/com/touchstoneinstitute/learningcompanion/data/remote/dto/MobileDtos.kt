package com.touchstoneinstitute.learningcompanion.data.remote.dto

data class LearnerOverviewResponse(
    val programName: String? = null,
    val programStatus: String? = null,
    val nextSession: SessionSummary? = null,
    val completedSessions: Int = 0,
    val totalSessions: Int = 0
)

data class SessionSummary(
    val id: String,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val location: String? = null,
    val type: String? = null
)

data class ScheduleResponse(
    val weekStart: String? = null,
    val weekEnd: String? = null,
    val days: List<ScheduleDay> = emptyList(),
    val lastUpdated: String? = null
)

data class ScheduleDay(
    val date: String,
    val dayName: String,
    val sessions: List<SessionSummary> = emptyList()
)

data class DeviceRegistrationRequest(
    val token: String,
    val platform: String = "android",
    val deviceModel: String? = null,
    val osVersion: String? = null
)

data class NotificationPreferencesResponse(
    val pushEnabled: Boolean = true,
    val scheduleReminders: Boolean = true,
    val orientationReminders: Boolean = true
)

data class UpdatePreferencesRequest(
    val pushEnabled: Boolean? = null,
    val scheduleReminders: Boolean? = null,
    val orientationReminders: Boolean? = null
)