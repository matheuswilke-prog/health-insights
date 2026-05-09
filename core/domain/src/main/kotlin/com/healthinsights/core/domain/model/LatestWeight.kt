package com.healthinsights.core.domain.model

import java.time.Instant

data class LatestWeight(
    val valueKg: Float,
    val measuredAt: Instant?,
)
