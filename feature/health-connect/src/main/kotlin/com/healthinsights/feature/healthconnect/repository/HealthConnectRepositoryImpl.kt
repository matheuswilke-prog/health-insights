package com.healthinsights.feature.healthconnect.repository

import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission
import com.healthinsights.core.domain.repository.HealthConnectRepository
import com.healthinsights.feature.healthconnect.HealthConnectManager
import javax.inject.Inject
import javax.inject.Singleton
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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

    override suspend fun getActiveCaloriesBurned(start: Instant, end: Instant): Float {
        val client = manager.client ?: return 0f
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                ),
            )
            // Log: count=${response.records.size} records read (no numeric values — CISO order)
            response.records.sumOf { it.energy.inKilocalories }.toFloat()
        } catch (_: Exception) {
            0f
        }
    }

    override suspend fun getNutritionCalories(start: Instant, end: Instant): Float {
        val client = manager.client ?: return 0f
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = NutritionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                ),
            )
            response.records
                .mapNotNull { it.energy?.inKilocalories }
                .sum()
                .toFloat()
        } catch (_: Exception) {
            0f
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
                    records.mapNotNull { it.energy?.inKilocalories }.sum().toFloat()
                }
                .filterKeys { it in days }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    override suspend fun getLatestWeightKg(): Float? {
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
            response.records.firstOrNull()?.weight?.inKilograms?.toFloat()
        } catch (_: Exception) {
            null
        }
    }
}
