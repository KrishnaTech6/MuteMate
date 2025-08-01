package com.krishna.mutemate.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.krishna.mutemate.broadcast_receiver.UnmuteReceiver
import com.krishna.mutemate.model.MuteSchedule
import com.krishna.mutemate.room.MuteScheduleDao
import com.krishna.mutemate.worker.MuteWorker
import java.util.concurrent.TimeUnit

fun scheduleWorker(
    muteDelay: Long,
    unmuteDelay: Long,
    schedule: MuteSchedule,
    workManager: WorkManager,
    context: Context
) {
    cancelUnmuteAlarm(schedule.id, context)
    val muteRequest = OneTimeWorkRequestBuilder<MuteWorker>()
        .setInitialDelay(muteDelay, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf(SCHEDULE_ID to schedule.id, DELAY to unmuteDelay))
        .build()
    workManager.enqueueUniqueWork("MuteTask_${schedule.id}", ExistingWorkPolicy.REPLACE, muteRequest)
    // alarm manager for exact time
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, UnmuteReceiver::class.java).apply {
        putExtra(SCHEDULE_ID, schedule.id)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        schedule.id.toInt(), // unique per schedule
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerAtMillis = System.currentTimeMillis() + unmuteDelay

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerAtMillis,
        pendingIntent
    )
}

fun cancelUnmuteAlarm(scheduleId: Long, context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, UnmuteReceiver::class.java).apply {
        putExtra(SCHEDULE_ID, scheduleId)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        scheduleId.toInt(), // same requestCode used in scheduling
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.cancel(pendingIntent)
}


fun checkExactAlarmPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return !alarmManager.canScheduleExactAlarms() // true if permission is missing
    }else return false
}

 fun sendUserToExactAlarmSettings(context: Context) {
     if(checkExactAlarmPermission(context)){
         // Send user to settings
         val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
             data = "package:${context.packageName}".toUri()
         }
         context.startActivity(intent)
     }
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

fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
    vectorDrawable.setBounds(
        0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight
    )
    val bitmap = createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun fetchPlaceDetails(
    placeId: String,
    context: Context,
    placesClient: PlacesClient,
    onLocationReady: (LatLng) -> Unit
) {
    val placeFields = listOf(Place.Field.LAT_LNG)
    val request = FetchPlaceRequest.builder(placeId, placeFields).build()

    placesClient.fetchPlace(request)
        .addOnSuccessListener { response ->
            val place = response.place
            place.latLng?.let { onLocationReady(it) }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Unable to get location", Toast.LENGTH_SHORT).show()
        }
}

suspend fun delSchedule(dao: MuteScheduleDao, context: Context, schedule: MuteSchedule){
    dao.delete(schedule)
    NotificationHelper.dismissNotification(context = context, schedule.id)
    if(dao.getRowCount()==0)
        dao.resetAutoIncrement()
    cancelMuteTasks(context , schedule)
}


fun openWebLink(context: Context, url: String){
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = url.toUri()
    }
    context.startActivity(intent)
}

fun sendEmailIntent(context: Context, email: String= "kris672dev@gmail.com", subject: String="MuteMate Feedback") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }

    try {
        context.startActivity(
            Intent.createChooser(intent, "Send email with")
        )
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
    }
}


fun shareApp(context: Context) {
    val packageName = context.packageName
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "Check out this app: https://play.google.com/store/apps/details?id=$packageName"
        )
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
}

fun rateApp(context: Context) {
    val packageName = context.packageName
    try {
        // Try to open in Play Store app
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "market://details?id=$packageName".toUri()
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (e: ActivityNotFoundException) {
        // If Play Store app not found, open in browser
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri()
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}


