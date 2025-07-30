package com.krishna.mutemate.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.krishna.mutemate.utils.DELAY
import com.krishna.mutemate.utils.MuteHelper
import com.krishna.mutemate.utils.MuteSettingsManager
import com.krishna.mutemate.utils.NotificationHelper
import com.krishna.mutemate.utils.SCHEDULE_ID
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
        val scheduleId = inputData.getLong(SCHEDULE_ID, -1)
        val delay = inputData.getLong(DELAY, -1)
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
        val formattedEnd = SimpleDateFormat("hh:mm a", Locale.getDefault())
            .format(System.currentTimeMillis() + delay)
        NotificationHelper.showPersistentNotification(
            context,
            scheduleTitle,
            "Schedule will end at $formattedEnd.",
            scheduleId
        )
        return Result.success()
    }
}
