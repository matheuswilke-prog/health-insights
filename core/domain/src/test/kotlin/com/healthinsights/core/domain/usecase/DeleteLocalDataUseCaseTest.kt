package com.healthinsights.core.domain.usecase

import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.domain.model.ConsentRecord
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.domain.model.UserProfile
import com.healthinsights.core.domain.repository.ConsentRepository
import com.healthinsights.core.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteLocalDataUseCaseTest {
    @Test
    fun invoke_clearsProfileAndConsentRecords() =
        runTest {
            val userProfileRepository =
                FakeUserProfileRepository(
                    UserProfile(
                        weightKg = 82.5f,
                        heightCm = 180,
                        ageYears = 32,
                        sex = BiologicalSex.MALE,
                        goal = UserGoal.LOSE,
                        dailyCalorieTarget = 2200,
                    ),
                )
            val consentRepository =
                FakeConsentRepository(
                    listOf(
                        ConsentRecord(
                            dataType = "calories",
                            granted = true,
                            grantedAt = 123L,
                            policyVersion = "consent-copy-v1.1",
                        ),
                    ),
                )
            val useCase =
                DeleteLocalDataUseCase(
                    userProfileRepository = userProfileRepository,
                    consentRepository = consentRepository,
                )

            useCase()

            assertNull(userProfileRepository.get().first())
            assertTrue(consentRepository.getAll().first().isEmpty())
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
}
