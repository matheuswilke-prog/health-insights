package com.healthinsights.core.domain.repository

import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission
import java.time.Instant
import java.time.LocalDate

interface HealthConnectRepository {
    fun getAvailability(): HealthConnectAvailability

    fun getRequiredPermissions(): Set<HealthDataPermission>

    suspend fun getGrantedPermissions(): Set<HealthDataPermission>

    // ── Data reads ────────────────────────────────────────────────────────────

    /**
     * Total active calories burned in [start, end).
     * Returns 0f when HC is unavailable or permission is not granted.
     */
    suspend fun getActiveCaloriesBurned(
        start: Instant,
        end: Instant,
    ): Float

    /**
     * Total calorie intake (from NutritionRecord) in [start, end).
     * Returns 0f when HC is unavailable, permission is not granted, or no food-log app synced.
     */
    suspend fun getNutritionCalories(
        start: Instant,
        end: Instant,
    ): Float

    /**
     * Active calories burned per day for the last 7 days (date → kcal).
     * Days with no data are omitted from the map.
     */
    suspend fun getActiveCaloriesByDay(days: List<LocalDate>): Map<LocalDate, Float>

    /**
     * Calorie intake per day for the last 7 days (date → kcal).
     * Days with no data are omitted from the map.
     */
    suspend fun getNutritionByDay(days: List<LocalDate>): Map<LocalDate, Float>

    /**
     * Most recent body weight reading from Health Connect (kg), or null if unavailable.
     */
    suspend fun getLatestWeightKg(): Float?
}
