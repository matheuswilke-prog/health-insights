package com.healthinsights.feature.dashboard

import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission
import com.healthinsights.core.domain.model.BalanceStatus
import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.domain.model.ConsentRecord
import com.healthinsights.core.domain.model.LatestWeight
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.domain.model.UserProfile
import com.healthinsights.core.domain.repository.ConsentRepository
import com.healthinsights.core.domain.repository.HealthConnectRepository
import com.healthinsights.core.domain.repository.HealthDataReadResult
import com.healthinsights.core.domain.repository.UserProfileRepository
import com.healthinsights.core.domain.usecase.CalculateBmrUseCase
import com.healthinsights.core.domain.usecase.CalculateDailyTargetUseCase
import com.healthinsights.core.domain.usecase.GetDailyBalanceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val zone = ZoneId.of("America/Sao_Paulo")
    private val profile = UserProfile(
        weightKg = 80f,
        heightCm = 180,
        ageYears = 30,
        sex = BiologicalSex.MALE,
        goal = UserGoal.LOSE,
        dailyCalorieTarget = 2200,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun content_complete_renders_balance_intake_expenditure_and_latest_weight() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()

            val state = vm.uiState.value as DashboardUiState.Content

            assertTrue(state.model.balance is BalanceUiModel.Available)
            assertEquals(BalanceStatus.Deficit, (state.model.balance as BalanceUiModel.Available).status)
            assertEquals(1850, (state.model.intake as IntakeUiModel.Available).kcal)
            assertEquals(620, (state.model.expenditure as ExpenditureUiModel.Available).activeKcal)
            assertEquals(82.4f, (state.model.weight as WeightUiModel.Available).kg)
        }

    @Test
    fun no_intake_does_not_show_balance_as_zero() =
        runTest(dispatcher) {
            val vm = viewModel(intake = 0f)
            advanceUntilIdle()

            val state = vm.uiState.value as DashboardUiState.Content

            assertTrue(state.model.balance is BalanceUiModel.Unavailable)
            assertEquals(IntakeUiModel.Empty, state.model.intake)
        }

    @Test
    fun partial_permission_does_not_read_calorie_balance() =
        runTest(dispatcher) {
            val health = FakeHealthConnectRepository(
                granted = setOf(HealthDataPermission.WEIGHT),
            )
            val vm = viewModel(health = health)
            advanceUntilIdle()

            val state = vm.uiState.value as DashboardUiState.Content

            assertTrue(state.model.balance is BalanceUiModel.Unavailable)
            assertEquals(IntakeUiModel.PermissionMissing, state.model.intake)
            assertEquals(0, health.activeReadCount)
            assertTrue(state.model.weight is WeightUiModel.Available)
        }

    @Test
    fun health_connect_unavailable_preserves_local_goal() =
        runTest(dispatcher) {
            val vm = viewModel(
                health = FakeHealthConnectRepository(
                    availability = HealthConnectAvailability.NotAvailable,
                ),
            )
            advanceUntilIdle()

            val state = vm.uiState.value as DashboardUiState.Content

            assertEquals(2200, state.model.goal.targetKcal)
            assertEquals(IntakeUiModel.HealthConnectUnavailable, state.model.intake)
            assertTrue(state.model.banner?.title?.contains("indisponível") == true)
        }

    @Test
    fun missing_local_profile_maps_to_local_invalid() =
        runTest(dispatcher) {
            val vm = viewModel(profile = null)
            advanceUntilIdle()

            assertEquals(DashboardUiState.LocalStateInvalid, vm.uiState.value)
        }

    @Test
    fun foreground_uses_fresh_cache() =
        runTest(dispatcher) {
            val health = FakeHealthConnectRepository()
            val vm = viewModel(health = health)
            advanceUntilIdle()

            vm.onForeground()
            advanceUntilIdle()

            assertEquals(1, health.activeReadCount)
            assertEquals(1, health.intakeReadCount)
        }

    private fun viewModel(
        profile: UserProfile? = this.profile,
        intake: Float = 1850f,
        active: Float = 620f,
        health: FakeHealthConnectRepository = FakeHealthConnectRepository(
            intake = intake,
            active = active,
        ),
        consents: List<ConsentRecord> = defaultConsents,
    ): DashboardViewModel {
        val profileRepository = FakeUserProfileRepository(profile)
        return DashboardViewModel(
            userProfileRepository = profileRepository,
            consentRepository = FakeConsentRepository(consents),
            healthConnectRepository = health,
            getDailyBalance = GetDailyBalanceUseCase(
                userProfileRepository = profileRepository,
                healthConnectRepository = health,
                calculateBmr = CalculateBmrUseCase(),
                calculateDailyTarget = CalculateDailyTargetUseCase(CalculateBmrUseCase()),
                zone = zone,
            ),
            calculateBmr = CalculateBmrUseCase(),
            calculateDailyTarget = CalculateDailyTargetUseCase(CalculateBmrUseCase()),
            zone = zone,
        )
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

    private class FakeHealthConnectRepository(
        private val availability: HealthConnectAvailability = HealthConnectAvailability.Available,
        private val granted: Set<HealthDataPermission> = setOf(
            HealthDataPermission.CALORIES_BURNED,
            HealthDataPermission.CALORIE_INTAKE,
            HealthDataPermission.WEIGHT,
        ),
        private val intake: Float = 1850f,
        private val active: Float = 620f,
        private val weight: LatestWeight? = LatestWeight(
            valueKg = 82.4f,
            measuredAt = Instant.parse("2026-05-08T12:00:00Z"),
        ),
    ) : HealthConnectRepository {
        var activeReadCount = 0
            private set
        var intakeReadCount = 0
            private set

        override fun getAvailability(): HealthConnectAvailability = availability

        override fun getRequiredPermissions(): Set<HealthDataPermission> = HealthDataPermission.entries.toSet()

        override suspend fun getGrantedPermissions(): Set<HealthDataPermission> = granted

        override suspend fun getActiveCaloriesBurned(
            start: Instant,
            end: Instant,
        ): Float = active

        override suspend fun getActiveCaloriesBurnedResult(
            start: Instant,
            end: Instant,
        ): HealthDataReadResult {
            activeReadCount += 1
            return HealthDataReadResult.Success(active)
        }

        override suspend fun getNutritionCalories(
            start: Instant,
            end: Instant,
        ): Float = intake

        override suspend fun getNutritionCaloriesResult(
            start: Instant,
            end: Instant,
        ): HealthDataReadResult {
            intakeReadCount += 1
            return HealthDataReadResult.Success(intake)
        }

        override suspend fun getActiveCaloriesByDay(days: List<LocalDate>): Map<LocalDate, Float> = emptyMap()

        override suspend fun getNutritionByDay(days: List<LocalDate>): Map<LocalDate, Float> = emptyMap()

        override suspend fun getLatestWeight(): LatestWeight? = weight
    }

    private companion object {
        val defaultConsents = listOf(
            ConsentRecord(dataType = "calories", granted = true, grantedAt = 0L, policyVersion = "test"),
            ConsentRecord(dataType = "weight", granted = true, grantedAt = 0L, policyVersion = "test"),
        )
    }
}
