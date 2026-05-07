package com.healthinsights.core.domain.healthconnect

sealed interface HealthConnectAvailability {
    data object Available : HealthConnectAvailability

    data object UpdateRequired : HealthConnectAvailability

    data object NotAvailable : HealthConnectAvailability
}
