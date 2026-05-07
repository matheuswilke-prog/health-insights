package com.healthinsights.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.healthinsights.core.database.dao.ConsentRecordDao
import com.healthinsights.core.database.entity.ConsentRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ConsentRecordDaoTest {

    private lateinit var database: HealthInsightsDatabase
    private lateinit var dao: ConsentRecordDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HealthInsightsDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
        dao = database.consentRecordDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insert_threeRecords_observeAllReturnsAll() = runTest {
        val records = listOf(
            buildConsentEntity(dataType = "CALORIES", granted = true, grantedAt = 1_000L),
            buildConsentEntity(dataType = "WEIGHT", granted = false, grantedAt = 2_000L),
            buildConsentEntity(dataType = "EXERCISE", granted = true, grantedAt = 3_000L),
        )

        records.forEach { dao.insert(it) }

        val result = dao.observeAll().first()
        assertEquals(3, result.size)
    }

    @Test
    fun insert_multipleRecordsForSameDataType_bothPersist() = runTest {
        // REPLACE strategy is on id (autoGenerate), not on dataType.
        // Two inserts with the same dataType but different ids should both persist.
        val first = buildConsentEntity(dataType = "CALORIES", granted = true, grantedAt = 1_000L)
        val second = buildConsentEntity(dataType = "CALORIES", granted = false, grantedAt = 2_000L)

        dao.insert(first)
        dao.insert(second)

        val result = dao.observeAll().first()
        assertEquals(2, result.size)
        assertTrue(result.all { it.dataType == "CALORIES" })
    }

    @Test
    fun clearAll_afterInserts_observeAllReturnsEmpty() = runTest {
        dao.insert(buildConsentEntity(dataType = "CALORIES", granted = true, grantedAt = 1_000L))
        dao.insert(buildConsentEntity(dataType = "WEIGHT", granted = true, grantedAt = 2_000L))

        dao.clearAll()

        val result = dao.observeAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun observeAll_recordsReturnedInDescendingGrantedAtOrder() = runTest {
        val older = buildConsentEntity(dataType = "WEIGHT", granted = true, grantedAt = 1_000L)
        val newer = buildConsentEntity(dataType = "CALORIES", granted = true, grantedAt = 3_000L)
        val middle = buildConsentEntity(dataType = "EXERCISE", granted = false, grantedAt = 2_000L)

        // Insert in non-chronological order to verify sorting
        dao.insert(older)
        dao.insert(middle)
        dao.insert(newer)

        val result = dao.observeAll().first()
        assertEquals(3, result.size)
        assertEquals(3_000L, result[0].grantedAt)
        assertEquals(2_000L, result[1].grantedAt)
        assertEquals(1_000L, result[2].grantedAt)
    }

    private fun buildConsentEntity(
        dataType: String,
        granted: Boolean,
        grantedAt: Long,
        policyVersion: String = "consent-copy-v1.1",
    ): ConsentRecordEntity = ConsentRecordEntity(
        id = 0, // autoGenerate
        dataType = dataType,
        granted = granted,
        grantedAt = grantedAt,
        policyVersion = policyVersion,
    )
}
