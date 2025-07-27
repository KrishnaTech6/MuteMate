package com.krishna.mutemate.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.krishna.mutemate.model.MuteSchedule
import com.krishna.mutemate.room.MuteScheduleDao
import com.krishna.mutemate.utils.NotificationHelper
import com.krishna.mutemate.utils.calculateDelay
import com.krishna.mutemate.utils.cancelMuteTasks
import com.krishna.mutemate.utils.scheduleWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
            if(scheduleList.isNotEmpty() && scheduleList.first().startTime == null && schedule.startTime==null)
                 deleteSchedule(scheduleList.first()) // delete the first item if its null as well as the new item is null this means that new duration was chosen by the user
            val insertedId = dao.insert(schedule)
            val updatedSchedule = schedule.copy(id = insertedId) // Update the schedule with the correct ID
            scheduleMuteTask(updatedSchedule)
        }
    }

    fun deleteSchedule(schedule: MuteSchedule) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(schedule)
            NotificationHelper.dismissNotification(context = app.applicationContext, schedule.id)
            if(dao.getRowCount()==0)
                dao.resetAutoIncrement()
            cancelMuteTasks(app.applicationContext , schedule)
        }
    }

    private fun scheduleMuteTask(schedule: MuteSchedule) {
        val muteDelay = calculateDelay(schedule.startTime)
        val unmuteDelay = calculateDelay(schedule.endTime)

        scheduleWorker(
            muteDelay = muteDelay,
            unmuteDelay = unmuteDelay,
            schedule = schedule,
            workManager = workManager
        )
    }
}
