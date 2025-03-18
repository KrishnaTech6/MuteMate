package com.example.mutemate.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mutemate.room.DatabaseProvider
import com.example.mutemate.utils.MuteHelper
import com.example.mutemate.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnmuteWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val scheduleId = inputData.getInt("schedule_id", -1)
        val isDnd = inputData.getBoolean("isDnd", true)
        val isVibrationMode = inputData.getBoolean("isVibration", true)
        if (scheduleId == -1) return Result.failure()
        val muteHelper = MuteHelper(context)

        if(isDnd || isVibrationMode)
            muteHelper.normalMode()
        else
            muteHelper.unmutePhone()

        Log.d("UnmuteWorker", "Phone unmuted")

        // Delete from DB using Singleton
        withContext(Dispatchers.IO) {
            DatabaseProvider.getDatabase(context).muteScheduleDao().deleteId(scheduleId)
        }
        NotificationHelper.dismissNotification(context, scheduleId)
        return Result.success()
    }
}