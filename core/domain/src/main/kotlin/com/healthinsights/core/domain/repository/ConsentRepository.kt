package com.healthinsights.core.domain.repository

import com.healthinsights.core.domain.model.ConsentRecord
import kotlinx.coroutines.flow.Flow

interface ConsentRepository {
    fun getAll(): Flow<List<ConsentRecord>>

    suspend fun save(record: ConsentRecord)

    suspend fun clearAll()
}
