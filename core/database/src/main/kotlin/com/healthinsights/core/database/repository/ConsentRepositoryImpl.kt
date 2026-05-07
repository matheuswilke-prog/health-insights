package com.healthinsights.core.database.repository

import com.healthinsights.core.database.dao.ConsentRecordDao
import com.healthinsights.core.database.entity.toDomain
import com.healthinsights.core.database.entity.toEntity
import com.healthinsights.core.domain.model.ConsentRecord
import com.healthinsights.core.domain.repository.ConsentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConsentRepositoryImpl @Inject constructor(
    private val dao: ConsentRecordDao,
) : ConsentRepository {

    override fun getAll(): Flow<List<ConsentRecord>> = dao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun save(record: ConsentRecord) {
        dao.insert(record.toEntity())
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}
