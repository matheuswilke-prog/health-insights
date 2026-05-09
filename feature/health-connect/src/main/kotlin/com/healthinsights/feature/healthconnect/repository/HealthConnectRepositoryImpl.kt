package com.healthinsights.feature.healthconnect.repository

import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission
import com.healthinsights.core.domain.model.LatestWeight
import com.healthinsights.core.domain.repository.HealthConnectRepository
import com.healthinsights.core.domain.repository.HealthDataReadResult
import com.healthinsights.feature.healthconnect.HealthConnectManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectRepositoryImpl @Inject constructor(
    private val manager: HealthConnectManager,
) : HealthConnectRepository {

    override fun getAvailability(): HealthConnectAvailability = manager.availability

    override fun getRequiredPermissions(): Set<HealthDataPermission> =
        HealthDataPermission.entries.toSet()

    override suspend fun getGrantedPermissions(): Set<HealthDataPermission> {
        val client = manager.client ?: return emptySet()
        val granted = client.permissionController.getGrantedPermissions()
        return HealthDataPermission.entries.filter { permission ->
            HealthConnectManager.permissionStringsFor(permission).all { it in granted }
        }.toSet()
    }

    override suspend fun getActiveCaloriesBurned(start: Instant, end: Instant): Float =
        getActiveCaloriesBurnedResult(start, end).kcalOrZero()

    override suspend fun getActiveCaloriesBurnedResult(
        start: Instant,
        end: Instant,
    ): HealthDataReadResult {
        val client = manager.client ?: return HealthDataReadResult.Unavailable
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                ),
            )
            HealthDataReadResult.Success(response.records.sumOf { it.energy.inKilocalories }.toFloat())
        } catch (e: Exception) {
            HealthDataReadResult.Error(e)
        }
    }

    override suspend fun getNutritionCalories(start: Instant, end: Instant): Float =
        getNutritionCaloriesResult(start, end).kcalOrZero()

    override suspend fun getNutritionCaloriesResult(
        start: Instant,
        end: Instant,
    ): HealthDataReadResult {
        val client = manager.client ?: return HealthDataReadResult.Unavailable
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = NutritionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                ),
            )
            response.records
                .mapNotNull { it.energy?.inKilocalories }
                .filter { it > 0.0 }
                .sum()
                .toFloat()
                .let(HealthDataReadResult::Success)
        } catch (e: Exception) {
            HealthDataReadResult.Error(e)
        }
    }

    override suspend fun getActiveCaloriesByDay(days: List<LocalDate>): Map<LocalDate, Float> {
        val client = manager.client ?: return emptyMap()
        if (days.isEmpty()) return emptyMap()

        val zone = ZoneId.systemDefault()
        val start = days.minOrNull()!!.atStartOfDay(zone).toInstant()
        val end = days.maxOrNull()!!.plusDays(1).atStartOfDay(zone).toInstant()

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                ),
            )
            response.records
                .groupBy { it.startTime.atZone(zone).toLocalDate() }
                .mapValues { (_, records) -> records.sumOf { it.energy.inKilocalories }.toFloat() }
                .filterKeys { it in days }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    override suspend fun getNutritionByDay(days: List<LocalDate>): Map<LocalDate, Float> {
        val client = manager.client ?: return emptyMap()
        if (days.isEmpty()) return emptyMap()

        val zone = ZoneId.systemDefault()
        val start = days.minOrNull()!!.atStartOfDay(zone).toInstant()
        val end = days.maxOrNull()!!.plusDays(1).atStartOfDay(zone).toInstant()

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = NutritionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                ),
            )
            response.records
                .groupBy { it.startTime.atZone(zone).toLocalDate() }
                .mapValues { (_, records) ->
                    records.mapNotNull { it.energy?.inKilocalories }
                        .filter { it > 0.0 }
                        .sum()
                        .toFloat()
                }
                .filterKeys { it in days }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    override suspend fun getLatestWeight(): LatestWeight? {
        val client = manager.client ?: return null
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        Instant.EPOCH,
                        Instant.now(),
                    ),
                    pageSize = 1,
                    ascendingOrder = false,
                ),
            )
            response.records.firstOrNull()?.let { record ->
                LatestWeight(
                    valueKg = record.weight.inKilograms.toFloat(),
                    measuredAt = record.time,
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun HealthDataReadResult.kcalOrZero(): Float = when (this) {
        is HealthDataReadResult.Success -> kcal
        is HealthDataReadResult.Unavailable,
        is HealthDataReadResult.Error,
        -> 0f
    }
}
