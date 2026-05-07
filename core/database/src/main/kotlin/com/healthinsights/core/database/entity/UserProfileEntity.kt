package com.healthinsights.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.domain.model.UserProfile

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val weightKg: Float,
    val heightCm: Int,
    val ageYears: Int,
    val sex: String,
    val goal: String,
    val dailyCalorieTarget: Int,
)

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    weightKg = weightKg,
    heightCm = heightCm,
    ageYears = ageYears,
    sex = BiologicalSex.valueOf(sex),
    goal = UserGoal.valueOf(goal),
    dailyCalorieTarget = dailyCalorieTarget,
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    id = 1,
    weightKg = weightKg,
    heightCm = heightCm,
    ageYears = ageYears,
    sex = sex.name,
    goal = goal.name,
    dailyCalorieTarget = dailyCalorieTarget,
)
