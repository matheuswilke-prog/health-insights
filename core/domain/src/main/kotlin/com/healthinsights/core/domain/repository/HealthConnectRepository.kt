package com.healthinsights.core.domain.repository

import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission
import com.healthinsights.core.domain.model.LatestWeight
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
     * Explicit active-calorie read result for UI flows that need to distinguish
     * "zero data" from "could not read Health Connect".
     */
    suspend fun getActiveCaloriesBurnedResult(
        start: Instant,
        end: Instant,
    ): HealthDataReadResult = HealthDataReadResult.Success(getActiveCaloriesBurned(start, end))

    /**
     * Total calorie intake (from NutritionRecord) in [start, end).
     * Returns 0f when HC is unavailable, permission is not granted, or no food-log app synced.
     */
    suspend fun getNutritionCalories(
        start: Instant,
        end: Instant,
    ): Float

    /**
     * Explicit nutrition read result for UI flows that need to distinguish
     * "no food-log data" from "could not read Health Connect".
     */
    suspend fun getNutritionCaloriesResult(
        start: Instant,
        end: Instant,
    ): HealthDataReadResult = HealthDataReadResult.Success(getNutritionCalories(start, end))

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
    suspend fun getLatestWeight(): LatestWeight?

    suspend fun getLatestWeightKg(): Float? = getLatestWeight()?.valueKg
}

sealed interface HealthDataReadResult {
    data class Success(val kcal: Float) : HealthDataReadResult

    data object Unavailable : HealthDataReadResult

    data class Error(val cause: Throwable) : HealthDataReadResult
}
