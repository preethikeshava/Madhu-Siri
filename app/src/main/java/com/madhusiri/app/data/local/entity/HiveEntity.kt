package com.madhusiri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hives")
data class HiveEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: String? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val healthScore: Float,
    val honeyProductionKg: Double,
    val lastInspectionDate: Long,
    val notes: String?,
    val isSynced: Boolean = false
)
