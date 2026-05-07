package com.healthinsights.feature.healthconnect.repository

import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission
import com.healthinsights.core.domain.repository.HealthConnectRepository
import com.healthinsights.feature.healthconnect.HealthConnectManager
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
}
