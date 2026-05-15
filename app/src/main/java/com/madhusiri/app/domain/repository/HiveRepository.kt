package com.madhusiri.app.domain.repository

import com.madhusiri.app.data.local.entity.HiveEntity
import kotlinx.coroutines.flow.Flow

interface HiveRepository {
    fun getAllHives(): Flow<List<HiveEntity>>
    fun getAverageHealthScore(): Flow<Double?>
    suspend fun insertHive(hive: HiveEntity)
    suspend fun getUnsyncedHives(): List<HiveEntity>
    suspend fun markAsSynced(hiveId: Int)
}
