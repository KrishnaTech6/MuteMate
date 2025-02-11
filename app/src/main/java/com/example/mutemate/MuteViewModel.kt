package com.example.mutemate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MuteViewModel(private val dao: MuteScheduleDao, private val context: Context) : ViewModel() {
    fun addSchedule(startTime: String, endTime: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(MuteSchedule(startTime = startTime, endTime = endTime))
            scheduleMuteTask(startTime, endTime)
        }
    }

    private fun scheduleMuteTask(startTime: String, endTime: String) {
        val workManager = WorkManager.getInstance(context)

        val muteRequest = OneTimeWorkRequestBuilder<MuteWorker>()
            .setInitialDelay(parseTime(startTime), TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()


        val unmuteRequest = OneTimeWorkRequestBuilder<UnmuteWorker>()
            .setInitialDelay(parseTime(endTime), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueue(muteRequest)
        workManager.enqueue(unmuteRequest)
    }

    private fun parseTime(time: String): Long {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance()
        targetTime.time = sdf.parse(time) ?: Date()

        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        return targetTime.timeInMillis - currentTime.timeInMillis
    }
}