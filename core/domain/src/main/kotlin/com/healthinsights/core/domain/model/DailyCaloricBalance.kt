package com.healthinsights.core.domain.model

import java.time.LocalDate

/**
 * Caloric balance for a single day.
 *
 * Formula:
 *   balance = intake − (bmr + activeBurned)
 *   balance < 0  → Deficit  (emagrecendo)
 *   balance > 0  → Surplus  (ganhando)
 *   |balance| ≤ 100 → Maintain (noise threshold)
 *   intake == 0  → NoIntakeData (food-log app not syncing yet)
 */
data class DailyCaloricBalance(
    val date: LocalDate,
    /** BMR calculated via Mifflin-St Jeor (kcal). */
    val bmr: Int,
    /** Active calories burned from Health Connect (kcal). */
    val activeBurned: Int,
    /** Calorie intake from Health Connect (kcal). 0 means no food-log data. */
    val intake: Int,
    /** Daily calorie target from UserProfile (kcal). */
    val target: Int,
    /** balance = intake − (bmr + activeBurned) */
    val balance: Int,
    val status: BalanceStatus,
) {
    companion object {
        /** ±100 kcal threshold below which we consider the day as "maintain" to suppress measurement noise. */
        const val MAINTAIN_THRESHOLD_KCAL = 100
    }
}

sealed interface BalanceStatus {
    /** User is eating less than they burn — losing weight trajectory. */
    data object Deficit : BalanceStatus

    /** User is eating more than they burn — gaining weight trajectory. */
    data object Surplus : BalanceStatus

    /** Balance within ±100 kcal of zero — maintenance. */
    data object Maintain : BalanceStatus

    /** No food-log data available for this day. Cannot compute balance. */
    data object NoIntakeData : BalanceStatus
}
