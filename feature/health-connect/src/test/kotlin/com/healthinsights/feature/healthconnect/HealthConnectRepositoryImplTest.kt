package com.healthinsights.feature.healthconnect

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission
import com.healthinsights.core.domain.repository.HealthDataReadResult
import com.healthinsights.feature.healthconnect.repository.HealthConnectRepositoryImpl
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class HealthConnectRepositoryImplTest {

    private lateinit var manager: HealthConnectManager
    private lateinit var mockClient: HealthConnectClient
    private lateinit var mockPermissionController: PermissionController
    private lateinit var repo: HealthConnectRepositoryImpl

    @Before
    fun setUp() {
        manager = mockk()
        mockClient = mockk()
        mockPermissionController = mockk()
        every { mockClient.permissionController } returns mockPermissionController
        repo = HealthConnectRepositoryImpl(manager)
    }

    @Test
    fun getAvailability_delegatesToManager() {
        every { manager.availability } returns HealthConnectAvailability.Available
        assertEquals(HealthConnectAvailability.Available, repo.getAvailability())
    }

    @Test
    fun getAvailability_returnsUpdateRequiredWhenManagerReportsSo() {
        every { manager.availability } returns HealthConnectAvailability.UpdateRequired
        assertEquals(HealthConnectAvailability.UpdateRequired, repo.getAvailability())
    }

    @Test
    fun getRequiredPermissions_returnsAllHealthDataPermissionEntries() {
        val required = repo.getRequiredPermissions()
        assertEquals(HealthDataPermission.entries.toSet(), required)
    }

    @Test
    fun getGrantedPermissions_returnsEmptySetWhenClientIsNull() = runTest {
        every { manager.client } returns null
        assertTrue(repo.getGrantedPermissions().isEmpty())
    }

    @Test
    fun getGrantedPermissions_returnsAllPermissionsWhenAllStringsGranted() = runTest {
        val allStrings = HealthConnectManager.requiredPermissionStrings()
        every { manager.client } returns mockClient
        coEvery { mockPermissionController.getGrantedPermissions() } returns allStrings

        val result = repo.getGrantedPermissions()

        assertEquals(HealthDataPermission.entries.toSet(), result)
    }

    @Test
    fun getGrantedPermissions_returnsOnlyCaloriesBurnedAndWeightWhenPartiallyGranted() = runTest {
        val partial = HealthConnectManager.permissionStringsFor(HealthDataPermission.CALORIES_BURNED) +
            HealthConnectManager.permissionStringsFor(HealthDataPermission.WEIGHT)
        every { manager.client } returns mockClient
        coEvery { mockPermissionController.getGrantedPermissions() } returns partial.toSet()

        val result = repo.getGrantedPermissions()

        assertEquals(setOf(HealthDataPermission.CALORIES_BURNED, HealthDataPermission.WEIGHT), result)
    }

    @Test
    fun getGrantedPermissions_excludesPermissionWhenItsStringIsMissing() = runTest {
        val weightOnly = HealthConnectManager.permissionStringsFor(HealthDataPermission.WEIGHT)
        every { manager.client } returns mockClient
        coEvery { mockPermissionController.getGrantedPermissions() } returns weightOnly

        val result = repo.getGrantedPermissions()

        assertTrue(HealthDataPermission.CALORIES_BURNED !in result)
    }

    @Test
    fun getGrantedPermissions_returnsEmptySetWhenNoStringsGranted() = runTest {
        every { manager.client } returns mockClient
        coEvery { mockPermissionController.getGrantedPermissions() } returns emptySet()

        assertTrue(repo.getGrantedPermissions().isEmpty())
    }

    @Test
    fun getActiveCaloriesBurnedResult_returnsUnavailableWhenClientIsNull() = runTest {
        every { manager.client } returns null

        val result = repo.getActiveCaloriesBurnedResult(Instant.EPOCH, Instant.EPOCH.plusSeconds(60))

        assertEquals(HealthDataReadResult.Unavailable, result)
    }

    @Test
    fun getNutritionCaloriesResult_returnsUnavailableWhenClientIsNull() = runTest {
        every { manager.client } returns null

        val result = repo.getNutritionCaloriesResult(Instant.EPOCH, Instant.EPOCH.plusSeconds(60))

        assertEquals(HealthDataReadResult.Unavailable, result)
    }
}
