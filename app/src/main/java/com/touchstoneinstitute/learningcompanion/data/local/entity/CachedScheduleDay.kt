package com.touchstoneinstitute.learningcompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_schedule_day")
data class CachedScheduleDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val weekName: String,
    val day: String,
    val period: String,
    val sessionName: String,
    val track: String,
    val group: String,
    val lastUpdated: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

