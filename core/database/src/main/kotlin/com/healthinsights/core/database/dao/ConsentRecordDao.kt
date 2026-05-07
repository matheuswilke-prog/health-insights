package com.healthinsights.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.healthinsights.core.database.entity.ConsentRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsentRecordDao {

    @Query("SELECT * FROM consent_record ORDER BY grantedAt DESC")
    fun observeAll(): Flow<List<ConsentRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ConsentRecordEntity)

    @Query("DELETE FROM consent_record")
    suspend fun clearAll()
}
