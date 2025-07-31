package com.krishna.mutemate.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.krishna.mutemate.utils.LocationMuteDaoEntryPoint
import com.krishna.mutemate.utils.MuteHelper
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private fun applyMuteForLocationId(context: Context, id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            // Access the DAO from Hilt
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                LocationMuteDaoEntryPoint::class.java
            )
            val locationMuteDao = entryPoint.locationMuteDao()
            val locationMute = locationMuteDao.getLocationById(id)
            locationMute?.let {
                if (it.muteOptions.isDnd) {
                    MuteHelper(context).dndModeOn()
                } else if (it.muteOptions.isVibrate) {
                    MuteHelper(context).vibrateModePhone()
                } else {
                    MuteHelper(context).mutePhone(
                        it.muteOptions.muteType.muteRingtone,
                        it.muteOptions.muteType.muteNotifications,
                        it.muteOptions.muteType.muteAlarm,
                        it.muteOptions.muteType.muteMedia
                    )
                }
            }
        }
    }
    private fun applyUnMuteForLocationId(context: Context, id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            // Access the DAO from Hilt
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                LocationMuteDaoEntryPoint::class.java
            )
            val locationMuteDao = entryPoint.locationMuteDao()
            val locationMute = locationMuteDao.getLocationById(id)
            locationMute?.let {
                if (it.muteOptions.isDnd || it.muteOptions.isVibrate) {
                    MuteHelper(context).normalMode()
                } else {
                    MuteHelper(context).unmutePhone()
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GeofenceReceiver", "Received geofence event")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        geofencingEvent?.let {
            if (it.hasError() == true) {
                Log.e("GeofenceReceiver", "Error: ${geofencingEvent.errorCode}")
                return
            }
        }

        val geofenceTransition = geofencingEvent?.geofenceTransition

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                // Mute logic
                val triggered = geofencingEvent.triggeringGeofences
                if (triggered != null) {
                    for (geofence in triggered) {
                        val id = geofence.requestId
                        Log.d("GeofenceReceiver", "Entered: $id")
                        applyMuteForLocationId(context, id.toLong())
                    }
                }
            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                // Unmute logic
                val triggered = geofencingEvent.triggeringGeofences
                if (triggered != null) {
                    for (geofence in triggered) {
                        val id = geofence.requestId
                        Log.d("GeofenceReceiver", "Exited: $id")
                        applyUnMuteForLocationId(context, id.toLong())
                    }
                }
            }
        }
    }
}