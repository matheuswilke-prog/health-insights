package com.healthinsights.core.domain.usecase

import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.domain.model.UserProfile
import javax.inject.Inject

/**
 * Calculates Basal Metabolic Rate (BMR) using the Mifflin-St Jeor equation.
 *
 * Formula:
 *   BMR = 10 * weightKg + 6.25 * heightCm − 5 * ageYears + s
 *   where s = +5 (Male) or −161 (Female)
 *
 * Result is rounded to the nearest integer (kcal/day).
 *
 * @throws IllegalArgumentException if weightKg, heightCm, or ageYears are ≤ 0.
 */
class CalculateBmrUseCase
    @Inject
    constructor() {
        operator fun invoke(profile: UserProfile): Int {
            require(profile.weightKg > 0f) { "weightKg must be > 0, got ${profile.weightKg}" }
            require(profile.heightCm > 0) { "heightCm must be > 0, got ${profile.heightCm}" }
            require(profile.ageYears > 0) { "ageYears must be > 0, got ${profile.ageYears}" }

            val sexOffset =
                when (profile.sex) {
                    BiologicalSex.MALE -> +5.0
                    BiologicalSex.FEMALE -> -161.0
                }

            val bmr =
                10.0 * profile.weightKg +
                    6.25 * profile.heightCm -
                    5.0 * profile.ageYears +
                    sexOffset

            return bmr.toInt()
        }
    }
