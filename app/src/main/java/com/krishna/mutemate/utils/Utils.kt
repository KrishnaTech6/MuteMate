package com.krishna.mutemate.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.provider.Settings
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.krishna.mutemate.model.MuteSchedule
import com.krishna.mutemate.worker.MuteWorker
import com.krishna.mutemate.worker.UnmuteWorker
import java.util.concurrent.TimeUnit

fun isBatteryLow(context: Context): Boolean {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

    return batteryLevel <= 15 // Consider battery low if 20% or less
}

fun scheduleWorker(
    muteDelay: Long,
    unmuteDelay: Long,
    schedule: MuteSchedule,
    workManager: WorkManager
) {
    val muteRequest = OneTimeWorkRequestBuilder<MuteWorker>()
        .setInitialDelay(muteDelay, TimeUnit.MILLISECONDS)
        .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
        .setInputData(workDataOf(SCHEDULE_ID to schedule.id, DELAY to unmuteDelay))
        .build()


    val unmuteRequest = OneTimeWorkRequestBuilder<UnmuteWorker>()
        .setInitialDelay(unmuteDelay, TimeUnit.MILLISECONDS)
        .setInputData(
            workDataOf(
                SCHEDULE_ID to schedule.id,
                IS_DND to schedule.muteOptions.isDnd,
                IS_VIBRATE to schedule.muteOptions.isVibrate
            )
        ).build()
    val muteTaskName = "MuteTask_${schedule.id}"
    val unmuteTaskName = "UnMuteTask_${schedule.id}"
    workManager.enqueueUniqueWork(muteTaskName, ExistingWorkPolicy.REPLACE, muteRequest)
    workManager.enqueueUniqueWork(unmuteTaskName, ExistingWorkPolicy.REPLACE, unmuteRequest)
}

fun cancelMuteTasks(context: Context, schedule: MuteSchedule) {
    val workManager = WorkManager.getInstance(context)
    workManager.cancelUniqueWork("MuteTask_${schedule.id}")
    if (schedule.muteOptions.isDnd || schedule.muteOptions.isVibrate)
        MuteHelper(context).normalMode()
    else
        MuteHelper(context).unmutePhone()
    workManager.cancelUniqueWork("UnMuteTask_${schedule.id}")
}

fun hasNotificationPolicyAccess(context: Context): Boolean {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return notificationManager.isNotificationPolicyAccessGranted
}

fun requestNotificationPolicyAccess(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    context.startActivity(intent)
}