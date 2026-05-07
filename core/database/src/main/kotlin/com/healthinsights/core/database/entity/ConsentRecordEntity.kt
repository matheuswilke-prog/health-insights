package com.healthinsights.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.healthinsights.core.domain.model.ConsentRecord

@Entity(
    tableName = "consent_record",
    indices = [Index(value = ["dataType"])],
)
data class ConsentRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dataType: String,
    val granted: Boolean,
    val grantedAt: Long,
    val policyVersion: String,
)

fun ConsentRecordEntity.toDomain(): ConsentRecord = ConsentRecord(
    id = id,
    dataType = dataType,
    granted = granted,
    grantedAt = grantedAt,
    policyVersion = policyVersion,
)

fun ConsentRecord.toEntity(): ConsentRecordEntity = ConsentRecordEntity(
    id = id,
    dataType = dataType,
    granted = granted,
    grantedAt = grantedAt,
    policyVersion = policyVersion,
)
