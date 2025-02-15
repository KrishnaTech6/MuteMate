package com.example.mutemate.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mutemate.room.DatabaseProvider
import com.example.mutemate.utils.MuteHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnmuteWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val scheduleId = inputData.getInt("schedule_id", -1)
        if (scheduleId == -1) return Result.failure()
        MuteHelper(context).unmutePhone()
        // Delete from DB using Singleton
        withContext(Dispatchers.IO) {
            DatabaseProvider.getDatabase(context).muteScheduleDao().deleteId(scheduleId)
        }
        return Result.success()
    }
}