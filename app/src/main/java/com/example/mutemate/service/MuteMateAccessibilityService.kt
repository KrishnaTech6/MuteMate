package com.example.mutemate.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.mutemate.utils.MuteSettingsManager
import com.example.mutemate.worker.MuteWorker
import com.example.mutemate.worker.UnmuteWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MuteMateAccessibilityService: AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var volumeDownCount = 0
    private val resetDelay = 800L // milliseconds to reset counter
    private val scope = CoroutineScope(Dispatchers.Main)

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
                val context = applicationContext
                val workManager = WorkManager.getInstance(context)
                val scheduleId = System.currentTimeMillis()
                val muteDelay = 0L
                val muteSettingsManager = MuteSettingsManager(context)
                val isDnd = muteSettingsManager.isDnd.first()
                val isVibration = muteSettingsManager.isVibrate.first()
                val unmuteDelay = muteSettingsManager.quickMuteDuration.first().toLong()

                val muteRequest = OneTimeWorkRequestBuilder<MuteWorker>()
                    .setInitialDelay(muteDelay, TimeUnit.MILLISECONDS)
                    .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                    .setInputData(workDataOf("schedule_id" to scheduleId))
                    .build()


                val unmuteRequest = OneTimeWorkRequestBuilder<UnmuteWorker>()
                    .setInitialDelay(unmuteDelay, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf(
                        "schedule_id" to scheduleId,
                        "isDnd" to isDnd,
                        "isVibration" to isVibration
                    )).build()
                val muteTaskName = "MuteTask_${scheduleId}"
                val unmuteTaskName = "UnmuteTask_${scheduleId}"
                workManager.enqueueUniqueWork(muteTaskName, ExistingWorkPolicy.REPLACE, muteRequest)
                workManager.enqueueUniqueWork(unmuteTaskName, ExistingWorkPolicy.REPLACE, unmuteRequest)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}