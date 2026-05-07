package com.healthinsights.core.domain.usecase

import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission
import com.healthinsights.core.domain.model.BalanceStatus
import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.domain.model.UserProfile
import com.healthinsights.core.domain.repository.HealthConnectRepository
import com.healthinsights.core.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class GetDailyBalanceUseCaseTest {
    // Canonical profile: 80 kg, 180 cm, 30 y, Male, MAINTAIN
    // BMR = 1780, TDEE/target = 2447
    private val baseProfile =
        UserProfile(
            weightKg = 80f,
            heightCm = 180,
            ageYears = 30,
            sex = BiologicalSex.MALE,
            goal = UserGoal.MAINTAIN,
            dailyCalorieTarget = 0,
        )

    private val today = LocalDate.of(2026, 5, 7)

    @Test
    fun deficit_large() =
        runTest {
            // intake=1500, active=300 → balance = 1500 - (1780+300) = -580 → Deficit
            val balance = useCase(intake = 1500f, active = 300f).invoke(today).first()
            assertEquals(-580, balance.balance)
            assertEquals(BalanceStatus.Deficit, balance.status)
            assertEquals(1780, balance.bmr)
            assertEquals(300, balance.activeBurned)
            assertEquals(1500, balance.intake)
        }

    @Test
    fun deficit_weak_within_threshold() =
        runTest {
            // intake=2030, active=200 → balance = 2030 - (1780+200) = 50 → Maintain (≤100)
            val balance = useCase(intake = 2030f, active = 200f).invoke(today).first()
            assertEquals(50, balance.balance)
            assertEquals(BalanceStatus.Maintain, balance.status)
        }

    @Test
    fun surplus() =
        runTest {
            // intake=3000, active=200 → balance = 3000 - (1780+200) = 1020 → Surplus
            val balance = useCase(intake = 3000f, active = 200f).invoke(today).first()
            assertEquals(1020, balance.balance)
            assertEquals(BalanceStatus.Surplus, balance.status)
        }

    @Test
    fun surplus_weak_within_threshold() =
        runTest {
            // intake=2040, active=200 → balance = 2040 - (1780+200) = 60 → Maintain (≤100)
            val balance = useCase(intake = 2040f, active = 200f).invoke(today).first()
            assertEquals(60, balance.balance)
            assertEquals(BalanceStatus.Maintain, balance.status)
        }

    @Test
    fun no_intake_data() =
        runTest {
            // intake=0 → NoIntakeData regardless of active
            val balance = useCase(intake = 0f, active = 500f).invoke(today).first()
            assertEquals(0, balance.intake)
            assertEquals(BalanceStatus.NoIntakeData, balance.status)
        }

    @Test
    fun no_intake_no_active() =
        runTest {
            // HC fully unavailable: intake=0, active=0 → NoIntakeData
            val balance = useCase(intake = 0f, active = 0f).invoke(today).first()
            assertEquals(BalanceStatus.NoIntakeData, balance.status)
        }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun useCase(
        intake: Float,
        active: Float,
    ): GetDailyBalanceUseCase {
        val profileRepo =
            object : UserProfileRepository {
                override fun get(): Flow<UserProfile?> = flowOf(baseProfile)

                override suspend fun save(profile: UserProfile) = Unit

                override suspend fun clear() = Unit
            }
        val hcRepo =
            object : HealthConnectRepository {
                override fun getAvailability() = HealthConnectAvailability.Available

                override fun getRequiredPermissions() = emptySet<HealthDataPermission>()

                override suspend fun getGrantedPermissions() = emptySet<HealthDataPermission>()

                override suspend fun getActiveCaloriesBurned(
                    start: Instant,
                    end: Instant,
                ) = active

                override suspend fun getNutritionCalories(
                    start: Instant,
                    end: Instant,
                ) = intake

                override suspend fun getActiveCaloriesByDay(days: List<LocalDate>) = emptyMap<LocalDate, Float>()

                override suspend fun getNutritionByDay(days: List<LocalDate>) = emptyMap<LocalDate, Float>()

                override suspend fun getLatestWeightKg(): Float? = null
            }
        return GetDailyBalanceUseCase(
            userProfileRepository = profileRepo,
            healthConnectRepository = hcRepo,
            calculateBmr = CalculateBmrUseCase(),
            calculateDailyTarget = CalculateDailyTargetUseCase(CalculateBmrUseCase()),
            zone = ZoneId.of("America/Sao_Paulo"),
        )
    }
}
