package com.krishna.mutemate.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.krishna.mutemate.model.MuteSchedule
import com.krishna.mutemate.room.MuteScheduleDao
import com.krishna.mutemate.utils.calculateDelay
import com.krishna.mutemate.utils.delSchedule
import com.krishna.mutemate.utils.getTimeUntilStart
import com.krishna.mutemate.utils.scheduleWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MuteViewModel @Inject constructor(
    private val dao: MuteScheduleDao,
    private val app: Application,
    private val workManager: WorkManager
) : ViewModel(){

    val allSchedules: Flow<List<MuteSchedule>> =
        dao.getSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSchedule(schedule: MuteSchedule) {
        viewModelScope.launch(Dispatchers.IO) {
            val scheduleList = allSchedules.firstOrNull()?: emptyList()
            if (scheduleList.any { it.startTime == schedule.startTime && it.endTime == schedule.endTime }) {
                return@launch
            }
            if(scheduleList.isNotEmpty() && schedule.startTime!! <= Date()){
                val alreadyRunning = scheduleList.find { it.startTime!! <= Date() }
                alreadyRunning?.let {
                    deleteSchedule(it)
                }
            }
            val insertedId = dao.insert(schedule)
            val updatedSchedule = schedule.copy(id = insertedId) // Update the schedule with the correct ID
            scheduleMuteTask(updatedSchedule)
        }
    }

    fun deleteSchedule(schedule: MuteSchedule) {
        viewModelScope.launch(Dispatchers.IO) {
            delSchedule(
                dao = dao,
                context = app.applicationContext,
                schedule = schedule
            )
        }
    }

    private fun scheduleMuteTask(schedule: MuteSchedule) {
        val muteDelay = calculateDelay(schedule.startTime)
        val unmuteDelay = calculateDelay(schedule.endTime)

        scheduleWorker(
            muteDelay = muteDelay,
            unmuteDelay = unmuteDelay,
            schedule = schedule,
            workManager = workManager,
            app.applicationContext
        )
    }

    fun formatTimeRemaining(timeRemaining: Int, isEnd: Boolean= false ): String {
        val endOrStart= if(!isEnd) "Starts" else "Ends"
        return when {
            timeRemaining >= 3600 -> "$endOrStart in ${timeRemaining / 3600}h ${timeRemaining % 3600 / 60}m".trimEnd()
            timeRemaining >= 120 -> "$endOrStart in ${timeRemaining / 60}m ${timeRemaining % 60}s"
            timeRemaining >= 60 -> "$endOrStart in 1m ${timeRemaining % 60}s"
            timeRemaining > 0 -> "$endOrStart in ${timeRemaining}s"
            else -> "Running"
        }
    }

    fun formatScheduleDuration(startTime: Date?, endTime: Date?): String {
        return try {
            if (getTimeUntilStart(startTime) <= 0) return ""

            val outputFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

            val startFormatted = outputFormat.format(startTime)
            val endFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(endTime!!)

            val isSameDay = SimpleDateFormat("dd MMM", Locale.getDefault()).format(startTime) ==
                    SimpleDateFormat("dd MMM", Locale.getDefault()).format(endTime)

            return if (isSameDay) "$startFormatted – $endFormatted"
            else "$startFormatted – ${outputFormat.format(endTime)}"
        } catch (e: Exception) {
            Log.e("ScheduleDuration", "Error formatting schedule duration: ${e.message}")
            return ""
        }
    }

    fun remainingTimeFlow(targetTime: Date?) = flow {
        var remaining = getTimeUntilStart(targetTime)
        while (remaining > 0) {
            emit(remaining)
            delay(if(remaining > 30*60 ) 60_000 else 1_000) // delay for 1 min if time > 30 min
            remaining = getTimeUntilStart(targetTime)
        }
    }
}
