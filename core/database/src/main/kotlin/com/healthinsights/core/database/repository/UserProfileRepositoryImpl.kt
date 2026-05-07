package com.healthinsights.core.database.repository

import com.healthinsights.core.database.dao.UserProfileDao
import com.healthinsights.core.database.entity.toDomain
import com.healthinsights.core.database.entity.toEntity
import com.healthinsights.core.domain.model.UserProfile
import com.healthinsights.core.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao,
) : UserProfileRepository {

    override fun get(): Flow<UserProfile?> = dao.observe().map { it?.toDomain() }

    override suspend fun save(profile: UserProfile) {
        dao.upsert(profile.toEntity())
    }

    override suspend fun clear() {
        dao.clear()
    }
}
