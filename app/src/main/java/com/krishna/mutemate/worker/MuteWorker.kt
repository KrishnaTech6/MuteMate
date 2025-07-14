package com.krishna.mutemate.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.krishna.mutemate.utils.MuteHelper
import com.krishna.mutemate.utils.MuteSettingsManager
import com.krishna.mutemate.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale

@HiltWorker
class MuteWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val scheduleId = inputData.getInt("schedule_id", -1)
        val delay = inputData.getLong("delay", -1)
        val muteHelper = MuteHelper(context)

        // Read settings synchronously
        val options = MuteSettingsManager(context).allMuteOptions.first()

        val scheduleTitle = when {
            options.isDnd -> {
                muteHelper.dndModeOn()
                "DND Mode schedule running."
            }
            options.isVibrate -> {
                muteHelper.vibrateModePhone()
                "Vibration Mode schedule running."
            }
            else -> {
                muteHelper.mutePhone(
                    options.muteType.muteRingtone,
                    options.muteType.muteNotifications,
                    options.muteType.muteAlarm,
                    options.muteType.muteMedia
                )
                "Mute schedule running."
            }
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
