package com.krishna.mutemate.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.krishna.mutemate.model.MuteSchedule
import com.krishna.mutemate.room.MuteScheduleDao
import com.krishna.mutemate.utils.SCHEDULE
import com.krishna.mutemate.utils.delSchedule
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class UnmuteWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dao: MuteScheduleDao
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {

        val scheduleString: String? = inputData.getString(SCHEDULE)
        val type = object: TypeToken<MuteSchedule>(){}.type
        val schedule = Gson().fromJson<MuteSchedule>(scheduleString, type)

        if (schedule.id.toInt() == 0) return Result.failure()

        // Delete from DB using Singleton
        withContext(Dispatchers.IO) {
            delSchedule(dao, applicationContext, schedule)
        }
        return Result.success()
    }
}