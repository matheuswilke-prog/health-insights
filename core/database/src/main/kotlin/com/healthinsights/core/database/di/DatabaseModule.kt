package com.healthinsights.core.database.di

import android.content.Context
import androidx.room.Room
import com.healthinsights.core.database.DatabaseKeyProvider
import com.healthinsights.core.database.HealthInsightsDatabase
import com.healthinsights.core.database.dao.ConsentRecordDao
import com.healthinsights.core.database.dao.UserProfileDao
import com.healthinsights.core.database.repository.ConsentRepositoryImpl
import com.healthinsights.core.database.repository.UserProfileRepositoryImpl
import com.healthinsights.core.domain.repository.ConsentRepository
import com.healthinsights.core.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: UserProfileRepositoryImpl,
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindConsentRepository(
        impl: ConsentRepositoryImpl,
    ): ConsentRepository

    companion object {

        @Provides
        @Singleton
        fun provideDatabase(
            @ApplicationContext context: Context,
        ): HealthInsightsDatabase {
            System.loadLibrary("sqlcipher")
            val keyProvider = DatabaseKeyProvider(context)
            val passphrase = keyProvider.getOrCreatePassphrase()
            val factory = SupportOpenHelperFactory(passphrase)
            passphrase.fill(0)

            return Room.databaseBuilder(
                context,
                HealthInsightsDatabase::class.java,
                "health_insights.db",
            )
                .openHelperFactory(factory)
                .build()
        }

        @Provides
        @Singleton
        fun provideUserProfileDao(db: HealthInsightsDatabase): UserProfileDao =
            db.userProfileDao()

        @Provides
        @Singleton
        fun provideConsentRecordDao(db: HealthInsightsDatabase): ConsentRecordDao =
            db.consentRecordDao()
    }
}
