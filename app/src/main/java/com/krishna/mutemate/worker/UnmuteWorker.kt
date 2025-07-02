package com.krishna.mutemate.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.krishna.mutemate.model.AllMuteOptions
import com.krishna.mutemate.model.MuteSchedule
import com.krishna.mutemate.room.AppDatabase
import com.krishna.mutemate.utils.IS_DND
import com.krishna.mutemate.utils.IS_VIBRATE
import com.krishna.mutemate.utils.MuteHelper
import com.krishna.mutemate.utils.NotificationHelper
import com.krishna.mutemate.utils.SCHEDULE_ID
import com.krishna.mutemate.utils.cancelMuteTasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class UnmuteWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val scheduleId = inputData.getInt(SCHEDULE_ID, -1)
        val isDnd = inputData.getBoolean(IS_DND, true)
        val isVibrationMode = inputData.getBoolean(IS_VIBRATE, true)

        if (scheduleId == -1) return Result.failure()
        val muteHelper = MuteHelper(context)

        if(isDnd || isVibrationMode)
            muteHelper.normalMode()
        else
            muteHelper.unmutePhone()

        // Delete from DB using Singleton
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getDatabase(context).muteScheduleDao()
            dao.deleteId(scheduleId)
            if(dao.getRowCount()==0) dao.resetAutoIncrement()
        }
        cancelMuteTasks(context, MuteSchedule(
            id = scheduleId,
            muteOptions = AllMuteOptions(isDnd, isVibrationMode),
            startTime = Date(), // NOT NEEDED
            endTime = Date() // Not Needed
        ))
        NotificationHelper.dismissNotification(context, scheduleId)
        return Result.success()
    }
}