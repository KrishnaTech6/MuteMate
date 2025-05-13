package com.example.mutemate.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mutemate.utils.MuteHelper
import com.example.mutemate.utils.MuteSettingsManager
import com.example.mutemate.utils.NotificationHelper
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale

class MuteWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        Log.d("MuteWorker", "Phone muted")
        val scheduleId = inputData.getInt("schedule_id", -1)
        val delay = inputData.getLong("delay", -1)
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
        var scheduleTitle = ""
        // Apply mute settings based on stored preferences
        if (isDnd) {
            muteHelper.dndModeOn()
            scheduleTitle = "DND Mode schedule running."
        }else if(isVibrationMode){
            muteHelper.vibrateModePhone()
            scheduleTitle = "Vibration Mode schedule running."
        }
        else {
           muteHelper.mutePhone(
               muteRingtone,
               muteNotifications,
               muteAlarms,
               muteMedia
           )
            scheduleTitle = "Mute schedule running."
        }
        val dateTime = System.currentTimeMillis() + delay
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedDateTime = dateFormat.format(dateTime)
        NotificationHelper.showPersistentNotification(
            context,
            scheduleTitle,
            "Schedule will end at $formattedDateTime.",
            scheduleId
        )
        return Result.success()
    }
}
