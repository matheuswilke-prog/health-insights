package com.healthinsights.core.database.security

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.healthinsights.core.database.HealthInsightsDatabase
import com.healthinsights.core.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class DatabaseEncryptionInstrumentedTest {

    private val correctPassphrase: ByteArray = "test-passphrase-32-bytes-padding!".toByteArray()
    private val wrongPassphrase: ByteArray = "totally-wrong-key-for-testing!!!!".toByteArray()
    private val dbName = "test_encryption.db"

    @Test
    fun openWithCorrectPassphrase_queriesSucceed() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val factory = SupportOpenHelperFactory(correctPassphrase)

        val db = Room.databaseBuilder(context, HealthInsightsDatabase::class.java, dbName)
            .openHelperFactory(factory)
            .allowMainThreadQueries()
            .build()

        try {
            val entity = UserProfileEntity(
                id = 1,
                weightKg = 70f,
                heightCm = 170,
                ageYears = 25,
                sex = "FEMALE",
                goal = "MAINTAIN",
                dailyCalorieTarget = 2000,
            )
            db.userProfileDao().upsert(entity)
            val result = db.userProfileDao().observe().first()
            assertNotNull("Expected to read back profile with correct passphrase", result)
        } finally {
            db.close()
            deleteDbFiles(context, dbName)
        }
    }

    @Test
    fun openWithWrongPassphrase_failsToReadData() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        val createDb = Room.databaseBuilder(context, HealthInsightsDatabase::class.java, dbName)
            .openHelperFactory(SupportOpenHelperFactory(correctPassphrase))
            .allowMainThreadQueries()
            .build()
        createDb.userProfileDao().upsert(
            UserProfileEntity(
                id = 1,
                weightKg = 80f,
                heightCm = 175,
                ageYears = 30,
                sex = "MALE",
                goal = "LOSE",
                dailyCalorieTarget = 1500,
            ),
        )
        createDb.close()

        val wrongDb = Room.databaseBuilder(context, HealthInsightsDatabase::class.java, dbName)
            .openHelperFactory(SupportOpenHelperFactory(wrongPassphrase))
            .allowMainThreadQueries()
            .build()

        var threwException = false
        try {
            wrongDb.userProfileDao().observe().first()
        } catch (_: Exception) {
            threwException = true
        } finally {
            try {
                wrongDb.close()
            } catch (_: Exception) {
                // SQLCipher may already have failed before fully opening the database.
            }
            deleteDbFiles(context, dbName)
        }

        assert(threwException) {
            "Expected SQLCipher to throw an exception when opened with wrong passphrase."
        }
    }

    @Test
    fun openWithoutSQLCipher_tableDoesNotExistOrThrows() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        val encryptedDb = Room.databaseBuilder(context, HealthInsightsDatabase::class.java, dbName)
            .openHelperFactory(SupportOpenHelperFactory(correctPassphrase))
            .allowMainThreadQueries()
            .build()
        encryptedDb.userProfileDao().upsert(
            UserProfileEntity(
                id = 1,
                weightKg = 70f,
                heightCm = 160,
                ageYears = 27,
                sex = "FEMALE",
                goal = "GAIN",
                dailyCalorieTarget = 2300,
            ),
        )
        encryptedDb.close()

        val plainDb = Room.databaseBuilder(context, HealthInsightsDatabase::class.java, dbName)
            .allowMainThreadQueries()
            .build()

        var result: UserProfileEntity? = null
        var threwException = false
        try {
            result = plainDb.userProfileDao().observe().first()
        } catch (_: Exception) {
            threwException = true
        } finally {
            try {
                plainDb.close()
            } catch (_: Exception) {
                // The unencrypted open may fail before Room fully initializes.
            }
            deleteDbFiles(context, dbName)
        }

        assert(threwException || result == null) {
            "Expected opening SQLCipher DB without key to fail or return no data, but got: $result"
        }
    }

    private fun deleteDbFiles(context: android.content.Context, name: String) {
        val dbFile = context.getDatabasePath(name)
        listOf(dbFile, File("${dbFile.path}-wal"), File("${dbFile.path}-shm")).forEach { file ->
            if (file.exists()) file.delete()
        }
    }
}
