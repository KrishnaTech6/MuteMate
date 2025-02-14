package com.example.mutemate

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnmuteWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val scheduleId = inputData.getInt("schedule_id", -1)

        if (scheduleId == -1) {
            return Result.failure()
        }
        MuteHelper(context).unmutePhone()
        // Delete schedule from DB
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "mute_schedule_db"
        ).build()

        val dao = database.muteScheduleDao()
        withContext(Dispatchers.IO){
            dao.deleteId(scheduleId)
        }

        return Result.success()
    }
}