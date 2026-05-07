package com.healthinsights.core.domain.model

data class UserProfile(
    val weightKg: Float,
    val heightCm: Int,
    val ageYears: Int,
    val sex: BiologicalSex,
    val goal: UserGoal,
    val dailyCalorieTarget: Int,
)
