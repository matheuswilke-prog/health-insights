package com.healthinsights.feature.healthconnect

import androidx.health.connect.client.HealthConnectClient
import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthConnectManagerTest {

    @Test
    fun `sdkStatusToAvailability returns Available for SDK_AVAILABLE`() {
        val result = HealthConnectManager.sdkStatusToAvailability(HealthConnectClient.SDK_AVAILABLE)
        assertEquals(HealthConnectAvailability.Available, result)
    }

    @Test
    fun `sdkStatusToAvailability returns UpdateRequired for provider update needed`() {
        val result = HealthConnectManager.sdkStatusToAvailability(
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED,
        )
        assertEquals(HealthConnectAvailability.UpdateRequired, result)
    }

    @Test
    fun `sdkStatusToAvailability returns NotAvailable for SDK_UNAVAILABLE`() {
        val result = HealthConnectManager.sdkStatusToAvailability(HealthConnectClient.SDK_UNAVAILABLE)
        assertEquals(HealthConnectAvailability.NotAvailable, result)
    }

    @Test
    fun `CALORIES_BURNED maps to two distinct permission strings`() {
        val strings = HealthConnectManager.permissionStringsFor(HealthDataPermission.CALORIES_BURNED)
        assertEquals(2, strings.size)
        assertTrue(strings.any { it.contains("TOTAL_CALORIES") })
        assertTrue(strings.any { it.contains("ACTIVE_CALORIES") })
    }

    @Test
    fun `CALORIE_INTAKE maps to one nutrition permission string`() {
        val strings = HealthConnectManager.permissionStringsFor(HealthDataPermission.CALORIE_INTAKE)
        assertEquals(1, strings.size)
        assertTrue(strings.first().contains("NUTRITION"))
    }

    @Test
    fun `WEIGHT maps to one weight permission string`() {
        val strings = HealthConnectManager.permissionStringsFor(HealthDataPermission.WEIGHT)
        assertEquals(1, strings.size)
        assertTrue(strings.first().contains("WEIGHT"))
    }

    @Test
    fun `EXERCISE maps to one exercise permission string`() {
        val strings = HealthConnectManager.permissionStringsFor(HealthDataPermission.EXERCISE)
        assertEquals(1, strings.size)
        assertTrue(strings.first().contains("EXERCISE"))
    }

    @Test
    fun `requiredPermissionStrings covers all HealthDataPermission entries`() {
        val required = HealthConnectManager.requiredPermissionStrings()
        val expected = HealthDataPermission.entries.flatMap {
            HealthConnectManager.permissionStringsFor(it)
        }.toSet()
        assertEquals(expected, required)
    }

    @Test
    fun `all permission strings are distinct`() {
        val required = HealthConnectManager.requiredPermissionStrings()
        assertEquals(required.size, required.toSet().size)
    }
}
