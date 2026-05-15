package com.madhusiri.app.data.repository

import com.madhusiri.app.data.local.dao.HiveDao
import com.madhusiri.app.data.local.entity.HiveEntity
import com.madhusiri.app.domain.repository.HiveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.madhusiri.app.services.SyncWorker

@Singleton
class HiveRepositoryImpl @Inject constructor(
    private val hiveDao: HiveDao,
    private val workManager: WorkManager
) : HiveRepository {
    override fun getAllHives(): Flow<List<HiveEntity>> {
        enqueueSyncWork()
        return hiveDao.getAllHives()
    }

    private fun enqueueSyncWork() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        workManager.enqueueUniqueWork("HIVE_SYNC", ExistingWorkPolicy.KEEP, syncRequest)
    }
    override fun getAverageHealthScore(): Flow<Double?> = hiveDao.getAverageHealthScore()
    override suspend fun insertHive(hive: HiveEntity) {
        hiveDao.insertHive(hive)
    }
    override suspend fun getUnsyncedHives(): List<HiveEntity> = hiveDao.getUnsyncedHives()
    override suspend fun markAsSynced(hiveId: Int) {
        hiveDao.markAsSynced(hiveId)
    }
}
