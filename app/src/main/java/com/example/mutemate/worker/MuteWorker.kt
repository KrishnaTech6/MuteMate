package com.example.mutemate.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mutemate.utils.MuteHelper
import com.example.mutemate.utils.MuteSettingsManager
import com.example.mutemate.utils.NotificationHelper
import kotlinx.coroutines.flow.first

class MuteWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        Log.d("MuteWorker", "Phone muted")
        val scheduleId = inputData.getInt("schedule_id", -1)
        val muteSettingsManager = MuteSettingsManager(context)
        val muteHelper = MuteHelper(context)

        // Read settings synchronously
        val isDnd = muteSettingsManager.isDnd.first()
        val muteRingtone = muteSettingsManager.muteRingtone.first()
        val isVibrationMode = muteSettingsManager.isVibrate.first()
        val muteNotifications =muteSettingsManager.muteNotifications.first()
        val muteAlarms = muteSettingsManager.muteAlarms.first()
        val muteMedia = muteSettingsManager.muteMedia.first()

        Log.d("MuteWorker", "Applying mute settings - DND: $isDnd, Ringtone: $muteRingtone, Notifications: $muteNotifications, Alarms: $muteAlarms, Media: $muteMedia")

        // Apply mute settings based on stored preferences
        if (isDnd) {
            muteHelper.dndModeOn()
        }else if(isVibrationMode){
            muteHelper.vibrateModePhone()
        }
        else {
           muteHelper.mutePhone(
               muteRingtone,
               muteNotifications,
               muteAlarms,
               muteMedia
           )
        }
        NotificationHelper.showPersistentNotification(
            context,
            "Schedule Running",
            "Your schedule is currently active.",
            scheduleId
        )
        return Result.success()
    }
}
