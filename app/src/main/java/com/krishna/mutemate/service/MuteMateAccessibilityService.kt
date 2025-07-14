package com.krishna.mutemate.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.work.WorkManager
import com.krishna.mutemate.model.AllMuteOptions
import com.krishna.mutemate.model.MuteSchedule
import com.krishna.mutemate.room.MuteScheduleDao
import com.krishna.mutemate.utils.MuteSettingsManager
import com.krishna.mutemate.utils.scheduleWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

@AndroidEntryPoint
class MuteMateAccessibilityService: AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var volumeDownCount = 0
    private val resetDelay = 800L // milliseconds to reset counter
    private val scope = CoroutineScope(Dispatchers.Main)
    @Inject lateinit var dao: MuteScheduleDao

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Optional: Handle UI events here if needed
    }

    override fun onInterrupt() {
        // Called when the system wants to stop your service
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.action == KeyEvent.ACTION_DOWN) {
            volumeDownCount++
            
            // Reset count after delay
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                volumeDownCount = 0
            }, resetDelay)
            
            // Check if we've reached 3 presses
            if (volumeDownCount == 3) {
                volumeDownCount = 0
                Toast.makeText(
                    applicationContext,
                    "Quick mute activated",
                    Toast.LENGTH_SHORT
                ).show()
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

                val muteDelay = 0L
                val unmuteDelay = muteSettingsManager.quickMuteDuration.first().toLong()*60*1000
                val schedule = MuteSchedule(
                    muteOptions = AllMuteOptions(options.isDnd, options.isVibrate),
                    startTime = Date(),
                    endTime = Date(System.currentTimeMillis() + unmuteDelay),
                )
                // Insert to DB using Singleton
                withContext(Dispatchers.IO) {
                    val updatedScheduleId = dao.insert(schedule)
                     schedule.copy(id = updatedScheduleId.toInt() )
                }

                scheduleWorker(
                    muteDelay = muteDelay,
                    unmuteDelay = unmuteDelay,
                    schedule = schedule,
                    workManager = workManager
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}