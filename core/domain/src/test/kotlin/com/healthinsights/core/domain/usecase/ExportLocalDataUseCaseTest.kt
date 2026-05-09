package com.healthinsights.core.domain.usecase

import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.domain.model.ConsentRecord
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.domain.model.UserProfile
import com.healthinsights.core.domain.repository.ConsentRepository
import com.healthinsights.core.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ExportLocalDataUseCaseTest {
    @Test
    fun invoke_exportsProfileAndConsentsAsJson() =
        runTest {
            val useCase =
                ExportLocalDataUseCase(
                    userProfileRepository =
                        FakeUserProfileRepository(
                            UserProfile(
                                weightKg = 82.5f,
                                heightCm = 180,
                                ageYears = 32,
                                sex = BiologicalSex.MALE,
                                goal = UserGoal.LOSE,
                                dailyCalorieTarget = 2200,
                            ),
                        ),
                    consentRepository =
                        FakeConsentRepository(
                            listOf(
                                ConsentRecord(
                                    dataType = "calories",
                                    granted = true,
                                    grantedAt = 123L,
                                    policyVersion = "consent-copy-v1.1",
                                ),
                            ),
                        ),
                    clock = fixedClock,
                )

            val json = useCase()

            assertTrue(json.contains("\"schemaVersion\": 1"))
            assertTrue(json.contains("\"exportedAt\": \"2026-05-09T12:00:00Z\""))
            assertTrue(json.contains("\"weightKg\": 82.5"))
            assertTrue(json.contains("\"heightCm\": 180"))
            assertTrue(json.contains("\"ageYears\": 32"))
            assertTrue(json.contains("\"sex\": \"MALE\""))
            assertTrue(json.contains("\"goal\": \"LOSE\""))
            assertTrue(json.contains("\"dailyCalorieTarget\": 2200"))
            assertTrue(json.contains("\"dataType\": \"calories\""))
            assertTrue(json.contains("\"policyVersion\": \"consent-copy-v1.1\""))
        }

    @Test
    fun invoke_exportsNullProfileWhenLocalProfileIsMissing() =
        runTest {
            val useCase =
                ExportLocalDataUseCase(
                    userProfileRepository = FakeUserProfileRepository(null),
                    consentRepository = FakeConsentRepository(emptyList()),
                    clock = fixedClock,
                )

            val json = useCase()

            assertTrue(json.contains("\"userProfile\": null"))
            assertTrue(json.contains("\"consents\": ["))
        }

    @Test
    fun invoke_doesNotIncludeDataTypesOutsideMvpCollectionScope() =
        runTest {
            val useCase =
                ExportLocalDataUseCase(
                    userProfileRepository = FakeUserProfileRepository(null),
                    consentRepository = FakeConsentRepository(emptyList()),
                    clock = fixedClock,
                )

            val json = useCase()

            listOf(
                "steps",
                "sleep",
                "heartRate",
                "email",
                "deviceId",
                "location",
                "foodName",
                "macros",
            ).forEach { forbiddenField ->
                assertFalse(json.contains(forbiddenField))
            }
        }

    private class FakeUserProfileRepository(
        profile: UserProfile?,
    ) : UserProfileRepository {
        private val flow = MutableStateFlow(profile)

        override fun get(): Flow<UserProfile?> = flow

        override suspend fun save(profile: UserProfile) {
            flow.value = profile
        }

        override suspend fun clear() {
            flow.value = null
        }
    }

    private class FakeConsentRepository(
        consents: List<ConsentRecord>,
    ) : ConsentRepository {
        private val flow = MutableStateFlow(consents)

        override fun getAll(): Flow<List<ConsentRecord>> = flow

        override suspend fun save(record: ConsentRecord) {
            flow.value = flow.value + record
        }

        override suspend fun clearAll() {
            flow.value = emptyList()
        }
    }

    private companion object {
        val fixedClock: Clock =
            Clock.fixed(
                Instant.parse("2026-05-09T12:00:00Z"),
                ZoneOffset.UTC,
            )
    }
}
