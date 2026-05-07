package com.healthinsights.core.domain.usecase

import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CalculateBmrUseCaseTest {
    private val useCase = CalculateBmrUseCase()

    // Canonical fixtures — verified against https://www.calculator.net/bmr-calculator.html
    // Mifflin-St Jeor: BMR = 10*w + 6.25*h - 5*a + s

    @Test
    fun male_typical() {
        // 80 kg, 180 cm, 30 y, M → 10*80 + 6.25*180 - 5*30 + 5 = 800+1125-150+5 = 1780
        val profile = profile(weight = 80f, height = 180, age = 30, sex = BiologicalSex.MALE)
        assertEquals(1780, useCase(profile))
    }

    @Test
    fun female_typical() {
        // 60 kg, 165 cm, 25 y, F → 10*60 + 6.25*165 - 5*25 - 161 = 600+1031.25-125-161 = 1345
        val profile = profile(weight = 60f, height = 165, age = 25, sex = BiologicalSex.FEMALE)
        assertEquals(1345, useCase(profile))
    }

    @Test
    fun male_heavy_older() {
        // 100 kg, 175 cm, 50 y, M → 1000+1093.75-250+5 = 1848
        val profile = profile(weight = 100f, height = 175, age = 50, sex = BiologicalSex.MALE)
        assertEquals(1848, useCase(profile))
    }

    @Test
    fun female_light_young() {
        // 45 kg, 155 cm, 18 y, F → 450+968.75-90-161 = 1167
        val profile = profile(weight = 45f, height = 155, age = 18, sex = BiologicalSex.FEMALE)
        assertEquals(1167, useCase(profile))
    }

    @Test
    fun edge_weight_zero_throws() {
        val profile = profile(weight = 0f, height = 170, age = 30, sex = BiologicalSex.MALE)
        assertThrows(IllegalArgumentException::class.java) { useCase(profile) }
    }

    @Test
    fun edge_height_zero_throws() {
        val profile = profile(weight = 70f, height = 0, age = 30, sex = BiologicalSex.MALE)
        assertThrows(IllegalArgumentException::class.java) { useCase(profile) }
    }

    @Test
    fun edge_age_zero_throws() {
        val profile = profile(weight = 70f, height = 170, age = 0, sex = BiologicalSex.MALE)
        assertThrows(IllegalArgumentException::class.java) { useCase(profile) }
    }

    @Test
    fun edge_negative_weight_throws() {
        val profile = profile(weight = -1f, height = 170, age = 30, sex = BiologicalSex.MALE)
        assertThrows(IllegalArgumentException::class.java) { useCase(profile) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun profile(
        weight: Float,
        height: Int,
        age: Int,
        sex: BiologicalSex,
    ) = UserProfile(
        weightKg = weight,
        heightCm = height,
        ageYears = age,
        sex = sex,
        goal = UserGoal.MAINTAIN,
        dailyCalorieTarget = 0,
    )
}
