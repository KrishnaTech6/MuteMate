package com.example.mutemate.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mutemate.utils.MuteHelper
import com.example.mutemate.utils.MuteSettingsManager
import kotlinx.coroutines.flow.first

class MuteWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        Log.d("MuteWorker", "Phone muted")
        val muteSettingsManager = MuteSettingsManager(context)
        val muteHelper = MuteHelper(context)

        // Read settings synchronously
        val isDnd = muteSettingsManager.isDnd.first()
        val muteRingtone = muteSettingsManager.muteRingtone.first()
        val muteNotifications =muteSettingsManager.muteNotifications.first()
        val muteAlarms = muteSettingsManager.muteAlarms.first()
        val muteMedia = muteSettingsManager.muteMedia.first()

        Log.d("MuteWorker", "Applying mute settings - DND: $isDnd, Ringtone: $muteRingtone, Notifications: $muteNotifications, Alarms: $muteAlarms, Media: $muteMedia")

        // Apply mute settings based on stored preferences
        if (isDnd) {
            muteHelper.dndModeOn()
        } else {
           muteHelper.mutePhone(
               muteRingtone,
               muteNotifications,
               muteAlarms,
               muteMedia
           )
        }
        return Result.success()
    }
}
