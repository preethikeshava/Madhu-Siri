package com.madhusiri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.madhusiri.app.data.local.entity.HiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHive(hive: HiveEntity): Long

    @Update
    suspend fun updateHive(hive: HiveEntity)

    @Query("SELECT * FROM hives ORDER BY lastInspectionDate DESC")
    fun getAllHives(): Flow<List<HiveEntity>>

    @Query("SELECT AVG(healthScore) FROM hives")
    fun getAverageHealthScore(): Flow<Double?>

    @Query("SELECT * FROM hives WHERE isSynced = 0")
    suspend fun getUnsyncedHives(): List<HiveEntity>

    @Query("UPDATE hives SET isSynced = 1 WHERE id = :hiveId")
    suspend fun markAsSynced(hiveId: Int)
}
