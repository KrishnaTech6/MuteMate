package com.example.mutemate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class MuteViewModel(private val dao: MuteScheduleDao, private val context: Context) : ViewModel() {
    val allSchedules: Flow<List<MuteSchedule>> = dao.getSchedules()

    fun addSchedule(schedule: MuteSchedule) {
        viewModelScope.launch(Dispatchers.IO) {
            val scheduleList = allSchedules.firstOrNull()?: emptyList()
            if (scheduleList.any { it.startTime == schedule.startTime && it.endTime == schedule.endTime }) {
                return@launch
            }
            if(scheduleList.isNotEmpty() && scheduleList.first().startTime.isEmpty()) deleteSchedule(scheduleList.first())
            val insertedId = dao.insert(schedule).toInt()
            val updatedSchedule = schedule.copy(id = insertedId) // Update the schedule with the correct ID
            scheduleMuteTask(updatedSchedule)
        }
    }

    fun deleteSchedule(schedule: MuteSchedule) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(schedule)
            cancelMuteTask(schedule)
        }
    }
    private fun cancelMuteTask(schedule: MuteSchedule) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork("MuteTask_${schedule.id}")
        MuteHelper(context).unmutePhone()
        workManager.cancelUniqueWork("UnmuteTask_${schedule.id}")
    }

    private fun scheduleMuteTask(schedule: MuteSchedule) {
        val workManager = WorkManager.getInstance(context)

        val muteDelay = calculateDelay(schedule.startTime)
        val unmuteDelay = calculateDelay(schedule.endTime)

        val muteRequest = OneTimeWorkRequestBuilder<MuteWorker>()
            .setInitialDelay(muteDelay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()

        val unmuteRequest = OneTimeWorkRequestBuilder<UnmuteWorker>()
            .setInitialDelay(unmuteDelay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("schedule_id" to schedule.id))
            .build()
        val muteTaskName = "MuteTask_${schedule.id}"
        val unmuteTaskName = "UnmuteTask_${schedule.id}"
        workManager.enqueueUniqueWork(muteTaskName, ExistingWorkPolicy.REPLACE, muteRequest)
        workManager.enqueueUniqueWork(unmuteTaskName, ExistingWorkPolicy.REPLACE, unmuteRequest)
    }

    private fun calculateDelay(time: String): Long {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance()

        try {
            val parsedTime = if (time.isEmpty()) return 0L else sdf.parse(time)
            targetTime.time = parsedTime

            // Ensure target time is on the same day or the next day if it's already passed
            targetTime.set(Calendar.YEAR, currentTime.get(Calendar.YEAR))
            targetTime.set(Calendar.MONTH, currentTime.get(Calendar.MONTH))
            targetTime.set(Calendar.DAY_OF_MONTH, currentTime.get(Calendar.DAY_OF_MONTH))

            if (targetTime.before(currentTime)) {
                targetTime.add(Calendar.DAY_OF_MONTH, 1)
            }

            val delay = targetTime.timeInMillis - currentTime.timeInMillis
            return delay
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0L
    }
}
