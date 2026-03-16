package com.touchstoneinstitute.learningcompanion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.touchstoneinstitute.learningcompanion.data.local.dao.OverviewDao
import com.touchstoneinstitute.learningcompanion.data.local.dao.ScheduleDayDao
import com.touchstoneinstitute.learningcompanion.data.local.entity.CachedOverview
import com.touchstoneinstitute.learningcompanion.data.local.entity.CachedScheduleDay

@Database(
    entities = [CachedOverview::class, CachedScheduleDay::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun overviewDao(): OverviewDao
    abstract fun scheduleDayDao(): ScheduleDayDao
}

