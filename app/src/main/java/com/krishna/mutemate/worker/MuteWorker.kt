package com.krishna.mutemate.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.krishna.mutemate.utils.MuteHelper
import com.krishna.mutemate.utils.MuteSettingsManager
import com.krishna.mutemate.utils.NotificationHelper
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale

class MuteWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val scheduleId = inputData.getInt("schedule_id", -1)
        val delay = inputData.getLong("delay", -1)
        val muteHelper = MuteHelper(context)

        // Read settings synchronously
        val options = MuteSettingsManager(context).allMuteOptions.first()

        var scheduleTitle = ""
        // Apply mute settings based on stored preferences
        if (options.isDnd) {
            muteHelper.dndModeOn()
            scheduleTitle = "DND Mode schedule running."
        }else if(options.isVibrate){
            muteHelper.vibrateModePhone()
            scheduleTitle = "Vibration Mode schedule running."
        }
        else {
           muteHelper.mutePhone(
               options.muteType.muteRingtone,
               options.muteType.muteNotifications,
               options.muteType.muteAlarm,
               options.muteType.muteMedia
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
