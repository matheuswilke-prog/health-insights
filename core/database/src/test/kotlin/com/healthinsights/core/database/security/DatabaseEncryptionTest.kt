package com.healthinsights.core.database.security

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.healthinsights.core.database.HealthInsightsDatabase
import com.healthinsights.core.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.Ignore
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Security tests verifying SQLCipher encryption behaviour.
 *
 * Note: The "open without SQLCipher" test uses Room's standard (unencrypted) builder
 * against the file produced by SQLCipher. It verifies the database cannot be queried
 * without the correct key by checking that the table does not exist or is unreadable.
 *
 * The in-file encryption test requires a real file-backed database, so these tests
 * use a named file database in the app's file system (via Robolectric). The key is
 * a simple deterministic byte array for test repeatability.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@Ignore("SQLCipher relies on native libraries; run DatabaseEncryptionInstrumentedTest on device/emulator.")
class DatabaseEncryptionTest {

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

        // First: create database with the correct passphrase and write data
        val correctFactory = SupportOpenHelperFactory(correctPassphrase)
        val createDb = Room.databaseBuilder(context, HealthInsightsDatabase::class.java, dbName)
            .openHelperFactory(correctFactory)
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

        // Second: try to open the same file with the wrong passphrase
        val wrongFactory = SupportOpenHelperFactory(wrongPassphrase)
        val wrongDb = Room.databaseBuilder(context, HealthInsightsDatabase::class.java, dbName)
            .openHelperFactory(wrongFactory)
            .allowMainThreadQueries()
            .build()

        var threwException = false
        try {
            // Accessing the DAO forces the database to open; SQLCipher should throw
            // a net.sqlcipher.database.SQLiteException on bad key
            wrongDb.userProfileDao().observe().first()
        } catch (e: Exception) {
            threwException = true
        } finally {
            try { wrongDb.close() } catch (_: Exception) { /* already failed */ }
            deleteDbFiles(context, dbName)
        }

        // SQLCipher must throw when given the wrong passphrase — it must never open silently
        assert(threwException) {
            "Expected SQLCipher to throw an exception when opened with wrong passphrase, but it opened silently"
        }
    }

    @Test
    fun openWithoutSQLCipher_tableDoesNotExistOrThrows() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        // Create the database with SQLCipher encryption
        val factory = SupportOpenHelperFactory(correctPassphrase)
        val encryptedDb = Room.databaseBuilder(context, HealthInsightsDatabase::class.java, dbName)
            .openHelperFactory(factory)
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

        // Attempt to open the same file using the standard Room builder (no SQLCipher)
        // This must fail or produce unreadable data
        val plainDb = Room.databaseBuilder(context, HealthInsightsDatabase::class.java, dbName)
            .allowMainThreadQueries()
            .build()

        var result: UserProfileEntity? = null
        var threwException = false
        try {
            result = plainDb.userProfileDao().observe().first()
        } catch (e: Exception) {
            threwException = true
        } finally {
            try { plainDb.close() } catch (_: Exception) { /* already failed */ }
            deleteDbFiles(context, dbName)
        }

        // Either an exception was thrown (database unreadable) or the result is null
        // (table not present / data uninterpretable). Both outcomes confirm encryption.
        assert(threwException || result == null) {
            "Expected opening SQLCipher DB without key to fail or return no data, but got: $result"
        }
    }

    private fun deleteDbFiles(context: android.content.Context, name: String) {
        val dbFile = context.getDatabasePath(name)
        listOf(dbFile, File("${dbFile.path}-wal"), File("${dbFile.path}-shm")).forEach { f ->
            if (f.exists()) f.delete()
        }
    }
}
