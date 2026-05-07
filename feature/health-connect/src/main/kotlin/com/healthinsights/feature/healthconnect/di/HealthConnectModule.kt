package com.healthinsights.feature.healthconnect.di

import com.healthinsights.core.domain.repository.HealthConnectRepository
import com.healthinsights.feature.healthconnect.repository.HealthConnectRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthConnectModule {

    @Binds
    @Singleton
    abstract fun bindHealthConnectRepository(
        impl: HealthConnectRepositoryImpl,
    ): HealthConnectRepository
}
