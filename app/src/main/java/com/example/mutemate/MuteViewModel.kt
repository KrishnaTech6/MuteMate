package com.example.mutemate

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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

        val muteDelay = calculateDelay(startTime)
        val unmuteDelay = calculateDelay(endTime)

        Log.d("MuteViewModel", "Mute delay: $muteDelay ms, Unmute delay: $unmuteDelay ms")

        val muteRequest = OneTimeWorkRequestBuilder<MuteWorker>()
            .setInitialDelay(muteDelay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()

        val unmuteRequest = OneTimeWorkRequestBuilder<UnmuteWorker>()
            .setInitialDelay(unmuteDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniqueWork("MuteTask", ExistingWorkPolicy.REPLACE, muteRequest)
        workManager.enqueueUniqueWork("UnmuteTask", ExistingWorkPolicy.REPLACE, unmuteRequest)
    }

    private fun calculateDelay(time: String): Long {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance()

        try {
            val parsedTime = sdf.parse(time) ?: return 0L
            targetTime.time = parsedTime

            // Ensure target time is on the same day or the next day if it's already passed
            targetTime.set(Calendar.YEAR, currentTime.get(Calendar.YEAR))
            targetTime.set(Calendar.MONTH, currentTime.get(Calendar.MONTH))
            targetTime.set(Calendar.DAY_OF_MONTH, currentTime.get(Calendar.DAY_OF_MONTH))

            if (targetTime.before(currentTime)) {
                targetTime.add(Calendar.DAY_OF_MONTH, 1)
            }

            val delay = targetTime.timeInMillis - currentTime.timeInMillis
            Log.d("MuteViewModel", "Calculated delay for $time: $delay milliseconds")
            return delay
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0L
    }
}
