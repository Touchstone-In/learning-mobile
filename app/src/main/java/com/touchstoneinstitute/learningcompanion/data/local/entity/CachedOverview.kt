package com.touchstoneinstitute.learningcompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_overview")
data class CachedOverview(
    @PrimaryKey val userId: String,
    val programName: String?,
    val programStatus: String?,
    val completedSessions: Int,
    val totalSessions: Int,
    val nextSessionId: String?,
    val nextSessionTitle: String?,
    val nextSessionDate: String?,
    val nextSessionStartTime: String?,
    val nextSessionEndTime: String?,
    val nextSessionLocation: String?,
    val nextSessionType: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

