package com.healthinsights.core.domain.model

data class ConsentRecord(
    val id: Long = 0,
    val dataType: String,
    val granted: Boolean,
    val grantedAt: Long,
    val policyVersion: String,
)
