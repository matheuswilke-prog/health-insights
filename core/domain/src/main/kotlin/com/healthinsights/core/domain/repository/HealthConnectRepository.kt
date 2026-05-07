package com.healthinsights.core.domain.repository

import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission

interface HealthConnectRepository {
    fun getAvailability(): HealthConnectAvailability
    fun getRequiredPermissions(): Set<HealthDataPermission>
    suspend fun getGrantedPermissions(): Set<HealthDataPermission>
}
