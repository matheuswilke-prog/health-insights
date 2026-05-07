package com.healthinsights.core.domain.usecase

import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateDailyTargetUseCaseTest {
    private val useCase = CalculateDailyTargetUseCase()

    // Base profile: 80 kg, 180 cm, 30 y, Male
    // BMR = 1780 kcal (verified in CalculateBmrUseCaseTest::male_typical)
    // TDEE = floor(1780 * 1.375) = floor(2447.5) = 2447

    @Test
    fun male_lose() {
        // 2447 - 500 = 1947
        val profile = profile(sex = BiologicalSex.MALE, goal = UserGoal.LOSE)
        assertEquals(1947, useCase(profile))
    }

    @Test
    fun male_maintain() {
        // 2447 + 0 = 2447
        val profile = profile(sex = BiologicalSex.MALE, goal = UserGoal.MAINTAIN)
        assertEquals(2447, useCase(profile))
    }

    @Test
    fun male_gain() {
        // 2447 + 300 = 2747
        val profile = profile(sex = BiologicalSex.MALE, goal = UserGoal.GAIN)
        assertEquals(2747, useCase(profile))
    }

    // Base profile: 60 kg, 165 cm, 25 y, Female
    // BMR = 1345 kcal (verified in CalculateBmrUseCaseTest::female_typical)
    // TDEE = floor(1345 * 1.375) = floor(1849.375) = 1849

    @Test
    fun female_lose() {
        // 1849 - 500 = 1349
        val profile =
            profile(
                weight = 60f,
                height = 165,
                age = 25,
                sex = BiologicalSex.FEMALE,
                goal = UserGoal.LOSE,
            )
        assertEquals(1349, useCase(profile))
    }

    @Test
    fun female_maintain() {
        // 1849 + 0 = 1849
        val profile =
            profile(
                weight = 60f,
                height = 165,
                age = 25,
                sex = BiologicalSex.FEMALE,
                goal = UserGoal.MAINTAIN,
            )
        assertEquals(1849, useCase(profile))
    }

    @Test
    fun female_gain() {
        // 1849 + 300 = 2149
        val profile =
            profile(
                weight = 60f,
                height = 165,
                age = 25,
                sex = BiologicalSex.FEMALE,
                goal = UserGoal.GAIN,
            )
        assertEquals(2149, useCase(profile))
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun profile(
        weight: Float = 80f,
        height: Int = 180,
        age: Int = 30,
        sex: BiologicalSex,
        goal: UserGoal,
    ) = UserProfile(
        weightKg = weight,
        heightCm = height,
        ageYears = age,
        sex = sex,
        goal = goal,
        dailyCalorieTarget = 0,
    )
}
