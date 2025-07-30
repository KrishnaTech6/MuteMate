package com.krishna.mutemate.service

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.work.WorkManager
import com.krishna.mutemate.model.AllMuteOptions
import com.krishna.mutemate.model.MuteSchedule
import com.krishna.mutemate.room.MuteScheduleDao
import com.krishna.mutemate.utils.MuteSettingsManager
import com.krishna.mutemate.utils.delSchedule
import com.krishna.mutemate.utils.scheduleWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class MuteMateAccessibilityService: AccessibilityService() {
    private var volumeDownCount = 0
    private val resetDelay = 800L // milliseconds to reset counter
    private val scope = CoroutineScope(Dispatchers.Main)
    @Inject lateinit var dao: MuteScheduleDao
    private var lastPressTime = 0L


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Optional: Handle UI events here if needed
    }

    override fun onInterrupt() {
        // Called when the system wants to stop your service
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.action == KeyEvent.ACTION_DOWN) {
            val now = System.currentTimeMillis()

            // Reset counter if time since last press > delay
            if (now - lastPressTime > resetDelay) {
                volumeDownCount = 0
            }

            lastPressTime = now
            volumeDownCount++
            
            // Check if we've reached 3 presses
            if (volumeDownCount == 3) {
                volumeDownCount = 0
                activateCustomDurationMute()
                return true
            }
        }
        return super.onKeyEvent(event)
    }
    
    private fun activateCustomDurationMute() {
        scope.launch {
            try {
                val workManager = WorkManager.getInstance(applicationContext)
                val muteSettingsManager = MuteSettingsManager(applicationContext)
                val options = muteSettingsManager.allMuteOptions.first()

                if(options.isValid()){
                    val muteDelay = 0L
                    val unmuteDelay = muteSettingsManager.quickMuteDuration.first().toLong()*60*1000
                    val schedule = MuteSchedule(
                        muteOptions = AllMuteOptions(options.isDnd, options.isVibrate, options.muteType),
                        startTime = Date(),
                        endTime = Date(System.currentTimeMillis() + unmuteDelay),
                    )
                    // Insert to DB using Singleton
                    withContext(Dispatchers.IO) {
                        //delete the schedules already running
                        val schedules= dao.getSchedules().first().forEach {
                            it.startTime?.let { startTime ->
                                if (startTime <= Date()){
                                    delSchedule(dao, applicationContext, it)
                                }
                            }
                        }
                        val scheduleId = dao.insert(schedule)
                        val newSchedule = schedule.copy(id = scheduleId)
                        scheduleWorker(
                            muteDelay = muteDelay,
                            unmuteDelay = unmuteDelay,
                            schedule = newSchedule,
                            workManager = workManager,
                            applicationContext
                        )
                    }
                    Toast.makeText(applicationContext, "Quick mute activated", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(applicationContext, "Failed: No MUTE MODE selected", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}