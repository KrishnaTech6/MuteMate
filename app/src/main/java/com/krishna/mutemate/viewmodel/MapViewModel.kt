package com.krishna.mutemate.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.krishna.mutemate.model.LocationMute
import com.krishna.mutemate.room.LocationMuteDao
import com.krishna.mutemate.utils.GeofenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(private val locationMuteDao: LocationMuteDao): ViewModel() {
    val allLocationMute: Flow<List<LocationMute>> = locationMuteDao.getAllLocationMutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertLocationMute(context: Context, locationMute: LocationMute) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Insert to DB
                val newId = locationMuteDao.insertLocationMute(locationMute)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return@launch
                }
                // Register Geofence using the returned ID
                val updatedLocationMute = locationMute.copy(id = newId)
                GeofenceHelper(context).apply {
                    val geofence = getGeofence(
                        id = newId.toString(),
                        latLng = updatedLocationMute.latLng!!,
                        radius = updatedLocationMute.radius?.toFloat() ?: 300f
                    )
                    val request = getGeofencingRequest(geofence)
                    val pendingIntent = getPendingIntent()

                    val geofencingClient = LocationServices.getGeofencingClient(context)
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return@launch
                    }
                    geofencingClient.addGeofences(request, pendingIntent)
                        .addOnSuccessListener {
                            Log.d("Geofence", "Geofence registered for ${newId}")
                        }
                        .addOnFailureListener {
                            Log.e("Geofence", "Failed to add: ${it.message}")
                        }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun deleteLocationMute(context: Context, locationMute: LocationMute) {
        viewModelScope.launch(Dispatchers.IO){
            try {
                locationMuteDao.deleteLocationMute(locationMute)
                // Remove Geofence
                val geofencingClient = LocationServices.getGeofencingClient(context)
                geofencingClient.removeGeofences(listOf(locationMute.id.toString()))
                    .addOnSuccessListener {
                        Log.d("Geofence", "Geofence removed for ${locationMute.id}")
                    }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

}