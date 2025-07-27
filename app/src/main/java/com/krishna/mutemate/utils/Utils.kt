package com.krishna.mutemate.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.os.BatteryManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.work.Constraints
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
import com.google.gson.Gson
import com.krishna.mutemate.model.MuteSchedule
import com.krishna.mutemate.room.MuteScheduleDao
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
                SCHEDULE to Gson().toJson(schedule),
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

