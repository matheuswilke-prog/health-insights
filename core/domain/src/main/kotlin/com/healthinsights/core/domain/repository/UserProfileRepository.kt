package com.healthinsights.core.domain.repository

import com.healthinsights.core.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun get(): Flow<UserProfile?>

    suspend fun save(profile: UserProfile)

    suspend fun clear()
}
