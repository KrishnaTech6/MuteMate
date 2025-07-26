package com.krishna.mutemate.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.krishna.mutemate.R
import com.krishna.mutemate.model.AllMuteOptions
import com.krishna.mutemate.model.LocationMute
import com.krishna.mutemate.ui.components.MuteOptionsDropDown
import com.krishna.mutemate.utils.MuteSettingsManager
import com.krishna.mutemate.utils.SharedPrefUtils.getCurrentLocation
import com.krishna.mutemate.utils.SharedPrefUtils.putCurrentLocation
import com.krishna.mutemate.utils.bitmapDescriptorFromVector
import com.krishna.mutemate.utils.fetchPlaceDetails
import com.krishna.mutemate.viewmodel.MapViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch


@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(modifier: Modifier = Modifier, viewmodel: MapViewModel = hiltViewModel()){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val options by MuteSettingsManager(context).allMuteOptions.collectAsState(AllMuteOptions(isDnd = true))


    val locationPermission = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    val backgroundLocationPermission = rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    var markerPosition by remember { mutableStateOf<LatLng?>( getCurrentLocation(context)) }
    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerPosition ?: LatLng(28.6139, 77.2090), 18f)
    }
    var markerType by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showMuteDialog by remember { mutableStateOf(false) }

    val suggestions = remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    val placesClient = remember { Places.createClient(context) }
    val token = remember { AutocompleteSessionToken.newInstance() }


    // Marker Options
    val markerTypes = listOf("Home", "Office", "Other")

    val searchQueryFlow = remember { MutableStateFlow("") }

    val muteList by viewmodel.allLocationMute.collectAsState(emptyList())


    LaunchedEffect(Unit) {
        if(!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
        }
        searchQueryFlow
            .debounce(300) // 300 ms debounce
            .filter { it.isNotBlank() }
            .distinctUntilChanged()
            .collect { query ->
                val request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setQuery(query)
                    .build()

                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        suggestions.value = response.autocompletePredictions
                    }
                    .addOnFailureListener {
                        suggestions.value = emptyList()
                    }
            }
    }

    // UI
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = locationPermission.status.isGranted),
            onMapClick = { latLng ->
                showMuteDialog = true
                markerPosition = latLng
            }
        ) {
            if(muteList.isNotEmpty()){
                muteList.forEach { mute ->
                    Marker(
                        state = MarkerState(position = mute.latLng!!),
                        title = mute.title,
                        icon = when(mute.title){
                            "Home" -> bitmapDescriptorFromVector(context, R.drawable.ic_home)
                            "Office" -> bitmapDescriptorFromVector(context, R.drawable.ic_office)
                            else -> bitmapDescriptorFromVector(context, R.drawable.ic_other)
                        }
                    )
                }
            }
        }
        if(isLoading) CircularProgressIndicator()
        // Search Bar
        Surface(
            tonalElevation = 3.dp,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Column {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            searchQueryFlow.value = query
                        },
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
                            markerPosition.let { position ->
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        position!!,
                                        16f
                                    )
                                )
                                searchQuery = ""
                                suggestions.value = emptyList()
                            }
                        }
                    }) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Search")
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()){
                    suggestions.value.forEach { prediction ->
                        Text(
                            text = prediction.getFullText(null).toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Log.d("Prediction", prediction.toString())
                                    // Fetch details
                                    fetchPlaceDetails(prediction.placeId, context, placesClient) { latLng ->
                                        scope.launch {
                                            markerPosition = latLng
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    latLng,
                                                    16f
                                                )
                                            )
                                            suggestions.value = emptyList()
                                            searchQuery = prediction.getPrimaryText(null).toString()
                                        }
                                    }
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        // current location FAB
        FloatingActionButton(
            onClick = {
                if (locationPermission.status.isGranted) {
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val latLng = LatLng(it.latitude, it.longitude)
                            markerPosition = latLng
                            putCurrentLocation(context = context, coordinates = latLng)
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
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Add Marker at Location")
        }


        // Marker Type Dialog
        if (showMuteDialog && markerPosition != null) {
            var radius = remember { mutableStateOf(500) }
            AlertDialog(
                onDismissRequest = { showMuteDialog = false },
                title = { Text("Mute at this Location?") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().padding(4.dp).verticalScroll(
                        rememberScrollState()
                    )){
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(text = "Radius:")
                            Slider(
                                value = radius.value.toFloat(),
                                onValueChange = { radius.value = it.toInt() },
                                valueRange = 100f..1000f,
                                steps = 900,
                                modifier = Modifier.padding(start = 8.dp).weight(1f)
                            )
                        }
                        MuteOptionsDropDown()
                        markerTypes.forEach { type ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        markerType = type
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = markerType == type,
                                    onClick = {
                                        markerType = type
                                    }
                                )
                                Text(type, Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMuteDialog = false
                        val muteLocation = LocationMute(
                            muteOptions = options,
                            latLng = markerPosition,
                            radius = radius.value,
                            title = markerType,
                            markerType = R.drawable.ic_home,
                        )
                        if(backgroundLocationPermission.status.isGranted)
                            viewmodel.insertLocationMute(context, muteLocation)
                        else
                            backgroundLocationPermission.launchPermissionRequest()
                    }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

