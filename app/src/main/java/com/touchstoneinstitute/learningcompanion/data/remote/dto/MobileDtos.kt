package com.touchstoneinstitute.learningcompanion.data.remote.dto

data class LearnerOverviewResponse(
    val programName: String? = null,
    val programType: String? = null,
    val applicationStatus: String? = null,
    val registrationStatus: String? = null,
    val nextSession: NextSessionSummary? = null,
    val keyDates: List<KeyDateSummary> = emptyList(),
)

data class KeyDateSummary(
    val label: String,
    val date: String,
)

data class NextSessionSummary(
    val sessionName: String? = null,
    val day: String? = null,
    val period: String? = null,
    val track: String? = null,
    val group: String? = null,
)

data class ScheduleResponse(
    val lastUpdated: String? = null,
    val weeks: List<ScheduleWeek> = emptyList(),
)

data class ScheduleWeek(
    val weekName: String,
    val days: List<ScheduleDay> = emptyList(),
)

data class ScheduleDay(
    val day: String,
    val period: String,
    val session: ScheduleEntry,
)

data class ScheduleEntry(
    val sessionName: String,
    val track: String,
    val group: String,
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

data class LearnerResultsResponse(
    val results: List<LearnerResultSummary> = emptyList(),
)

data class LearnerResultSummary(
    val id: String,
    val attendanceStatus: String? = null,
    val asyncStatus: String? = null,
    val associatedMedicalSchool: String? = null,
    val postgraduateTrainingProgram: String? = null,
    val name: String? = null,
    val comment: String? = null,
    val publishedAt: String? = null,
)