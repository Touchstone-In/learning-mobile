package com.touchstoneinstitute.learningcompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_overview")
data class CachedOverview(
    @PrimaryKey val userId: String,
    val programName: String?,
    val programType: String?,
    val applicationStatus: String?,
    val registrationStatus: String?,
    val nextSessionName: String?,
    val nextSessionDay: String?,
    val nextSessionPeriod: String?,
    val nextSessionTrack: String?,
    val nextSessionGroup: String?,
    val keyDatesJson: String,
    val cachedAt: Long = System.currentTimeMillis()
)

