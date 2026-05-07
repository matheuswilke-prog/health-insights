package com.healthinsights.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.healthinsights.core.database.dao.ConsentRecordDao
import com.healthinsights.core.database.dao.UserProfileDao
import com.healthinsights.core.database.entity.ConsentRecordEntity
import com.healthinsights.core.database.entity.UserProfileEntity

@Database(
    entities = [UserProfileEntity::class, ConsentRecordEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class HealthInsightsDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao

    abstract fun consentRecordDao(): ConsentRecordDao
}
