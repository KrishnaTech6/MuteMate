package com.krishna.mutemate.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.krishna.mutemate.room.MuteScheduleDao
import com.krishna.mutemate.utils.SCHEDULE_ID
import com.krishna.mutemate.utils.delSchedule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UnmuteReceiver: BroadcastReceiver() {
    @Inject lateinit var dao: MuteScheduleDao
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(SCHEDULE_ID, -1)
        if (scheduleId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            val schedule = dao.getScheduleById(scheduleId)
            if (schedule != null) {
                delSchedule(dao, context, schedule)
            }
        }
    }
}