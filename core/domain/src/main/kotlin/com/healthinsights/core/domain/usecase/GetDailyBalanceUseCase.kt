package com.healthinsights.core.domain.usecase

import com.healthinsights.core.domain.model.BalanceStatus
import com.healthinsights.core.domain.model.DailyCaloricBalance
import com.healthinsights.core.domain.model.DailyCaloricBalance.Companion.MAINTAIN_THRESHOLD_KCAL
import com.healthinsights.core.domain.repository.HealthConnectRepository
import com.healthinsights.core.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Returns a [Flow] emitting the [DailyCaloricBalance] for [date].
 *
 * Algorithm:
 *   1. Load UserProfile from Room (via [UserProfileRepository]).
 *   2. Calculate BMR with Mifflin-St Jeor (via [CalculateBmrUseCase]).
 *   3. Read active calories burned + calorie intake from Health Connect for [date].
 *   4. Compute balance = intake − (bmr + activeBurned).
 *   5. Derive [BalanceStatus] from balance value (±100 kcal maintain threshold).
 *
 * Edge cases:
 *   - intake == 0 → status = [BalanceStatus.NoIntakeData] (user hasn't synced a food-log app).
 *   - HC unavailable → activeBurned = 0, intake = 0 → NoIntakeData.
 */
class GetDailyBalanceUseCase
    @Inject
    constructor(
        private val userProfileRepository: UserProfileRepository,
        private val healthConnectRepository: HealthConnectRepository,
        private val calculateBmr: CalculateBmrUseCase,
        private val calculateDailyTarget: CalculateDailyTargetUseCase,
        private val zone: ZoneId,
    ) {
        operator fun invoke(date: LocalDate): Flow<DailyCaloricBalance> =
            flow {
                val profile =
                    userProfileRepository.get().first()
                        ?: error("UserProfile not set — onboarding must complete before reading balance.")

                val dayStart: Instant = date.atStartOfDay(zone).toInstant()
                val dayEnd: Instant = date.plusDays(1).atStartOfDay(zone).toInstant()

                val bmr = calculateBmr(profile)
                val target = calculateDailyTarget(profile)
                val activeBurned = healthConnectRepository.getActiveCaloriesBurned(dayStart, dayEnd).toInt()
                val intake = healthConnectRepository.getNutritionCalories(dayStart, dayEnd).toInt()

                val balance = intake - (bmr + activeBurned)

                val status: BalanceStatus =
                    when {
                        intake == 0 -> BalanceStatus.NoIntakeData
                        balance < -MAINTAIN_THRESHOLD_KCAL -> BalanceStatus.Deficit
                        balance > MAINTAIN_THRESHOLD_KCAL -> BalanceStatus.Surplus
                        else -> BalanceStatus.Maintain
                    }

                emit(
                    DailyCaloricBalance(
                        date = date,
                        bmr = bmr,
                        activeBurned = activeBurned,
                        intake = intake,
                        target = target,
                        balance = balance,
                        status = status,
                    ),
                )
            }
    }
