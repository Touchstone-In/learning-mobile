package com.touchstoneinstitute.learningcompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_schedule_day")
data class CachedScheduleDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val date: String,
    val dayName: String,
    val sessionId: String,
    val sessionTitle: String,
    val sessionDate: String,
    val sessionStartTime: String,
    val sessionEndTime: String,
    val sessionLocation: String?,
    val sessionType: String?,
    val weekStart: String?,
    val weekEnd: String?,
    val lastUpdated: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

