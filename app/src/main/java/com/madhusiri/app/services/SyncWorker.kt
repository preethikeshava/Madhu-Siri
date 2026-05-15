package com.madhusiri.app.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.FirebaseDatabase
import com.madhusiri.app.domain.repository.HiveRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: HiveRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val unsyncedLogs = repository.getUnsyncedHives()
        if (unsyncedLogs.isEmpty()) return Result.success()

        val database = FirebaseDatabase.getInstance().getReference("hive_logs")

        return try {
            unsyncedLogs.forEach { log ->
                database.child(log.id.toString()).setValue(log).await()
                repository.markAsSynced(log.id)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
