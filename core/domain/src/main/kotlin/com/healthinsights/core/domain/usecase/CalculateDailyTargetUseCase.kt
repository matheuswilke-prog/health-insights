package com.healthinsights.core.domain.usecase

import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.domain.model.UserProfile

/**
 * Calculates the daily calorie target based on BMR, sedentary multiplier, and goal.
 *
 * Formula:
 *   TDEE = BMR * 1.375  (sedentary multiplier — refined in later versions via HC activity data)
 *   target = TDEE + goalAdjustment
 *   where:
 *     LOSE     → −500 kcal/day (~0.5 kg/week)
 *     MAINTAIN → ±0 kcal/day
 *     GAIN     → +300 kcal/day (~0.3 kg/week)
 *
 * @throws IllegalArgumentException if profile fields are invalid (delegated to CalculateBmrUseCase).
 */
class CalculateDailyTargetUseCase(
    private val calculateBmr: CalculateBmrUseCase = CalculateBmrUseCase(),
) {
    operator fun invoke(profile: UserProfile): Int {
        val bmr = calculateBmr(profile)
        val tdee = (bmr * SEDENTARY_MULTIPLIER).toInt()
        val adjustment =
            when (profile.goal) {
                UserGoal.LOSE -> GOAL_LOSE_ADJUSTMENT
                UserGoal.MAINTAIN -> 0
                UserGoal.GAIN -> GOAL_GAIN_ADJUSTMENT
            }
        return tdee + adjustment
    }

    private companion object {
        const val SEDENTARY_MULTIPLIER = 1.375
        const val GOAL_LOSE_ADJUSTMENT = -500
        const val GOAL_GAIN_ADJUSTMENT = +300
    }
}
