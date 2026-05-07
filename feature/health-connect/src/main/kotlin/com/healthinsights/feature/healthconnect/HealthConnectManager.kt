package com.healthinsights.feature.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val availability: HealthConnectAvailability
        get() = sdkStatusToAvailability(HealthConnectClient.getSdkStatus(context))

    val client: HealthConnectClient?
        get() = if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else null

    fun createPermissionRequestContract() =
        PermissionController.createRequestPermissionResultContract()

    companion object {
        fun sdkStatusToAvailability(sdkStatus: Int): HealthConnectAvailability = when (sdkStatus) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.Available
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectAvailability.UpdateRequired
            else -> HealthConnectAvailability.NotAvailable
        }

        fun requiredPermissionStrings(): Set<String> = HealthDataPermission.entries
            .flatMap { permissionStringsFor(it) }
            .toSet()

        fun permissionStringsFor(permission: HealthDataPermission): Set<String> = when (permission) {
            HealthDataPermission.CALORIES_BURNED -> setOf(
                HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
                HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            )
            HealthDataPermission.CALORIE_INTAKE ->
                setOf(HealthPermission.getReadPermission(NutritionRecord::class))
            HealthDataPermission.WEIGHT ->
                setOf(HealthPermission.getReadPermission(WeightRecord::class))
            HealthDataPermission.EXERCISE ->
                setOf(HealthPermission.getReadPermission(ExerciseSessionRecord::class))
        }
    }
}
