package com.touchstoneinstitute.learningcompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.touchstoneinstitute.learningcompanion.data.local.entity.CachedOverview

@Dao
interface OverviewDao {

    @Query("SELECT * FROM cached_overview WHERE userId = :userId LIMIT 1")
    suspend fun getOverview(userId: String): CachedOverview?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverview(overview: CachedOverview)

    @Query("DELETE FROM cached_overview WHERE userId = :userId")
    suspend fun deleteOverview(userId: String)

    @Query("DELETE FROM cached_overview")
    suspend fun deleteAll()
}

