package com.touchstoneinstitute.learningcompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.touchstoneinstitute.learningcompanion.data.local.entity.CachedScheduleDay

@Dao
interface ScheduleDayDao {

    @Query("SELECT * FROM cached_schedule_day WHERE userId = :userId ORDER BY id ASC")
    suspend fun getScheduleDays(userId: String): List<CachedScheduleDay>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(days: List<CachedScheduleDay>)

    @Query("DELETE FROM cached_schedule_day WHERE userId = :userId")
    suspend fun deleteForUser(userId: String)

    @Transaction
    suspend fun replaceForUser(userId: String, days: List<CachedScheduleDay>) {
        deleteForUser(userId)
        insertAll(days)
    }

    @Query("DELETE FROM cached_schedule_day")
    suspend fun deleteAll()
}

