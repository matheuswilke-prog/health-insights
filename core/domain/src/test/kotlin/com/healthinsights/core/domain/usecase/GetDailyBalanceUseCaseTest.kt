package com.healthinsights.core.domain.usecase

import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission
import com.healthinsights.core.domain.model.BalanceStatus
import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.domain.model.UserProfile
import com.healthinsights.core.domain.repository.HealthConnectRepository
import com.healthinsights.core.domain.repository.HealthDataReadResult
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
            val balance = useCase(intake = 1500f, active = 300f).invoke(today).first()
            assertEquals(-580, balance.balance)
            assertEquals(BalanceStatus.Deficit, balance.status)
            assertEquals(1780, balance.bmr)
            assertEquals(300, balance.activeBurned)
            assertEquals(1500, balance.intake)
        }

    @Test
    fun deficit_boundary_is_maintenance() =
        runTest {
            val balance = useCase(intake = 1730f, active = 200f).invoke(today).first()
            assertEquals(-250, balance.balance)
            assertEquals(BalanceStatus.Maintain, balance.status)
        }

    @Test
    fun deficit_below_boundary() =
        runTest {
            val balance = useCase(intake = 1729f, active = 200f).invoke(today).first()
            assertEquals(-251, balance.balance)
            assertEquals(BalanceStatus.Deficit, balance.status)
        }

    @Test
    fun surplus_boundary_is_maintenance() =
        runTest {
            val balance = useCase(intake = 2230f, active = 200f).invoke(today).first()
            assertEquals(250, balance.balance)
            assertEquals(BalanceStatus.Maintain, balance.status)
        }

    @Test
    fun surplus_above_boundary() =
        runTest {
            val balance = useCase(intake = 2231f, active = 200f).invoke(today).first()
            assertEquals(251, balance.balance)
            assertEquals(BalanceStatus.Surplus, balance.status)
        }

    @Test
    fun no_intake_data() =
        runTest {
            val balance = useCase(intake = 0f, active = 500f).invoke(today).first()
            assertEquals(0, balance.intake)
            assertEquals(BalanceStatus.NoIntakeData, balance.status)
        }

    @Test
    fun negative_intake_is_no_intake_data() =
        runTest {
            val balance = useCase(intake = -1f, active = 500f).invoke(today).first()
            assertEquals(-1, balance.intake)
            assertEquals(BalanceStatus.NoIntakeData, balance.status)
        }

    @Test
    fun health_connect_unavailable() =
        runTest {
            val balance =
                useCase(
                    intake = 0f,
                    active = 0f,
                    readResult = HealthDataReadResult.Unavailable,
                ).invoke(today).first()

            assertEquals(BalanceStatus.HealthConnectUnavailable, balance.status)
        }

    private fun useCase(
        intake: Float,
        active: Float,
        readResult: HealthDataReadResult? = null,
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

                override suspend fun getActiveCaloriesBurnedResult(
                    start: Instant,
                    end: Instant,
                ) = readResult ?: HealthDataReadResult.Success(active)

                override suspend fun getNutritionCalories(
                    start: Instant,
                    end: Instant,
                ) = intake

                override suspend fun getNutritionCaloriesResult(
                    start: Instant,
                    end: Instant,
                ) = readResult ?: HealthDataReadResult.Success(intake)

                override suspend fun getActiveCaloriesByDay(days: List<LocalDate>) = emptyMap<LocalDate, Float>()

                override suspend fun getNutritionByDay(days: List<LocalDate>) = emptyMap<LocalDate, Float>()

                override suspend fun getLatestWeight() = null
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
