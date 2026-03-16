package com.touchstoneinstitute.learningcompanion.di

import android.content.Context
import androidx.room.Room
import com.touchstoneinstitute.learningcompanion.data.local.AppDatabase
import com.touchstoneinstitute.learningcompanion.data.local.dao.OverviewDao
import com.touchstoneinstitute.learningcompanion.data.local.dao.ScheduleDayDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tsin_learning_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideOverviewDao(database: AppDatabase): OverviewDao {
        return database.overviewDao()
    }

    @Provides
    fun provideScheduleDayDao(database: AppDatabase): ScheduleDayDao {
        return database.scheduleDayDao()
    }
}

