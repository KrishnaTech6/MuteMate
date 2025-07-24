package com.krishna.mutemate.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.krishna.mutemate.R
import com.krishna.mutemate.utils.bitmapDescriptorFromVector
import kotlinx.coroutines.launch
import java.util.Locale


@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview(showBackground = true)
fun MapScreen(modifier: Modifier = Modifier){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationPermission = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(28.6139, 77.2090), 12f)
    }
    var markerPosition by remember { mutableStateOf<LatLng?>(null) }
    var markerType by remember { mutableStateOf("Home") }
    var searchQuery by remember { mutableStateOf("") }
    var showTypeDialog by remember { mutableStateOf(false) }

    // Marker Options
    val markerTypes = listOf("Home", "Office", "Other")

    // UI
    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = locationPermission.status.isGranted),
            onMapClick = { latLng ->
                markerPosition = latLng
                showTypeDialog = true
            }
        ) {
            markerPosition?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = markerType,
                    icon = when(markerType) {
                        "Home" -> bitmapDescriptorFromVector(context, R.drawable.ic_home)
                        "Office" -> bitmapDescriptorFromVector(context, R.drawable.ic_office)
                        else -> bitmapDescriptorFromVector(context, R.drawable.ic_other)
                    }
                )
            }
        }

        // Search Bar
        Surface(
            tonalElevation = 3.dp,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth().align(Alignment.TopCenter)
        ) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search location...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {
                    scope.launch {
                        runCatching {
                            val geo = Geocoder(context, Locale.getDefault())
                            val address = geo.getFromLocationName(searchQuery, 1)?.firstOrNull()
                            address?.let {
                                val latLng = LatLng(it.latitude, it.longitude)
                                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                                markerPosition = latLng
                                showTypeDialog = true
                            }
                        }.onFailure {
                            Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Search")
                }
            }
        }

        // Add marker at current location FAB
        FloatingActionButton(
            onClick = {
                if (locationPermission.status.isGranted) {
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val latLng = LatLng(it.latitude, it.longitude)
                            markerPosition = latLng
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                                    durationMs = 1000
                                )
                            }
                        } ?: run {
                            Toast.makeText(context, "Unable to get location", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    locationPermission.launchPermissionRequest()
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) {
            Icon(Icons.Default.AddLocation, contentDescription = "Add Marker at Location")
        }


        // Marker Type Dialog
        if (showTypeDialog && markerPosition != null) {
            AlertDialog(
                onDismissRequest = { showTypeDialog = false },
                title = { Text("Select Marker Type") },
                text = {
                    Column {
                        markerTypes.forEach { type ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        markerType = type
                                        showTypeDialog = false
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                RadioButton(
                                    selected = markerType == type,
                                    onClick = {
                                        markerType = type
                                        showTypeDialog = false
                                    }
                                )
                                Text(type, Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTypeDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

