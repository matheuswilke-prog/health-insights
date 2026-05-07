package com.healthinsights.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.healthinsights.core.database.dao.UserProfileDao
import com.healthinsights.core.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UserProfileDaoTest {

    private lateinit var database: HealthInsightsDatabase
    private lateinit var dao: UserProfileDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HealthInsightsDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
        dao = database.userProfileDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsert_fullProfile_observeReturnsSameProfile() = runTest {
        val entity = buildProfileEntity(weightKg = 80f, heightCm = 175, ageYears = 30, sex = "MALE", goal = "LOSE", dailyCalorieTarget = 1800)

        dao.upsert(entity)

        val result = dao.observe().first()
        assertEquals(entity, result)
    }

    @Test
    fun upsert_secondCallWithId1_replacesFirstRecord() = runTest {
        val first = buildProfileEntity(weightKg = 80f, heightCm = 175, ageYears = 30, sex = "MALE", goal = "LOSE", dailyCalorieTarget = 1800)
        val second = buildProfileEntity(weightKg = 75f, heightCm = 170, ageYears = 28, sex = "FEMALE", goal = "MAINTAIN", dailyCalorieTarget = 2000)

        dao.upsert(first)
        dao.upsert(second)

        val result = dao.observe().first()
        assertEquals(second, result)
    }

    @Test
    fun clear_afterUpsert_observeReturnsNull() = runTest {
        val entity = buildProfileEntity(weightKg = 70f, heightCm = 165, ageYears = 25, sex = "FEMALE", goal = "GAIN", dailyCalorieTarget = 2300)

        dao.upsert(entity)
        dao.clear()

        val result = dao.observe().first()
        assertNull(result)
    }

    @Test
    fun upsert_dailyCalorieTargetPersistsCorrectly() = runTest {
        // TMB for a 30-year-old 80 kg 175 cm male (Mifflin-St Jeor):
        // TMB = (10 * 80) + (6.25 * 175) - (5 * 30) + 5 = 800 + 1093.75 - 150 + 5 = 1748.75 ≈ 1749
        // With LOSE modifier (-500): 1749 - 500 = 1249 (example value)
        val expectedTarget = 1249
        val entity = buildProfileEntity(
            weightKg = 80f,
            heightCm = 175,
            ageYears = 30,
            sex = "MALE",
            goal = "LOSE",
            dailyCalorieTarget = expectedTarget,
        )

        dao.upsert(entity)

        val result = dao.observe().first()
        assertEquals(expectedTarget, result?.dailyCalorieTarget)
    }

    private fun buildProfileEntity(
        weightKg: Float,
        heightCm: Int,
        ageYears: Int,
        sex: String,
        goal: String,
        dailyCalorieTarget: Int,
    ): UserProfileEntity = UserProfileEntity(
        id = 1,
        weightKg = weightKg,
        heightCm = heightCm,
        ageYears = ageYears,
        sex = sex,
        goal = goal,
        dailyCalorieTarget = dailyCalorieTarget,
    )
}
