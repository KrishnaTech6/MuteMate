package com.example.mutemate.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.mutemate.model.MuteSchedule
import com.example.mutemate.room.MuteScheduleDao
import com.example.mutemate.utils.MuteHelper
import com.example.mutemate.utils.NotificationHelper
import com.example.mutemate.utils.calculateDelay
import com.example.mutemate.worker.MuteWorker
import com.example.mutemate.worker.UnmuteWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MuteViewModel(private val dao: MuteScheduleDao, application: Application) : AndroidViewModel(application) {
    val allSchedules: Flow<List<MuteSchedule>> = dao.getSchedules()

    fun addSchedule(schedule: MuteSchedule) {
        viewModelScope.launch(Dispatchers.IO) {
            val scheduleList = allSchedules.firstOrNull()?: emptyList()
            if (scheduleList.any { it.startTime == schedule.startTime && it.endTime == schedule.endTime }) {
                return@launch
            }
            if(scheduleList.isNotEmpty() && scheduleList.first().startTime == null && schedule.startTime==null)
                 deleteSchedule(scheduleList.first()) // delete the first item if its null as well as the new item is null this means that new duration was chosen by the user
            val insertedId = dao.insert(schedule).toInt()
            val updatedSchedule = schedule.copy(id = insertedId) // Update the schedule with the correct ID
            scheduleMuteTask(updatedSchedule)
        }
    }

    fun deleteSchedule(schedule: MuteSchedule) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(schedule)
            val context = getApplication<Application>().applicationContext
            NotificationHelper.dismissNotification(context = context, schedule.id)
            if(dao.getRowCount()==0)
                dao.resetAutoIncrement()
            cancelMuteTask(schedule)
        }
    }

    private fun cancelMuteTask(schedule: MuteSchedule) {
        val context = getApplication<Application>().applicationContext
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork("MuteTask_${schedule.id}")
        if(schedule.isDnd || schedule.isVibrationMode)
            MuteHelper(context).normalMode()
        else
            MuteHelper(context).unmutePhone()
        workManager.cancelUniqueWork("UnmuteTask_${schedule.id}")
    }

    private fun scheduleMuteTask(schedule: MuteSchedule) {
        val context = getApplication<Application>().applicationContext
        val workManager = WorkManager.getInstance(context)

        val muteDelay = calculateDelay(schedule.startTime)
        val unmuteDelay = calculateDelay(schedule.endTime)
        Log.d("MuteViewModel", "muteDelay: $muteDelay, unmuteDelay: $unmuteDelay")

        val muteRequest = OneTimeWorkRequestBuilder<MuteWorker>()
            .setInitialDelay(muteDelay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .setInputData(workDataOf("schedule_id" to schedule.id))
            .build()

        val unmuteRequest = OneTimeWorkRequestBuilder<UnmuteWorker>()
            .setInitialDelay(unmuteDelay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "schedule_id" to schedule.id,
                "isDnd" to schedule.isDnd,
                "isVibration" to schedule.isVibrationMode
            )).build()
        val muteTaskName = "MuteTask_${schedule.id}"
        val unmuteTaskName = "UnmuteTask_${schedule.id}"
        workManager.enqueueUniqueWork(muteTaskName, ExistingWorkPolicy.REPLACE, muteRequest)
        workManager.enqueueUniqueWork(unmuteTaskName, ExistingWorkPolicy.REPLACE, unmuteRequest)
    }
}
