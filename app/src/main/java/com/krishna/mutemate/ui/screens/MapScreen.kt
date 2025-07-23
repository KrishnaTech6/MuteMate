package com.krishna.mutemate.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview(showBackground = true)
fun MapScreen(modifier: Modifier = Modifier){
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Coming Soon!", style = MaterialTheme.typography.displayMedium)
    }
}

// {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    val locationPermission = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
//    var cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(LatLng(28.6139, 77.2090), 12f)
//    }
//    var markerPosition by remember { mutableStateOf<LatLng?>(null) }
//    var markerType by remember { mutableStateOf("Home") }
//    var searchQuery by remember { mutableStateOf("") }
//    var showTypeDialog by remember { mutableStateOf(false) }
//
//    // Marker Options
//    val markerTypes = listOf("Home", "Office", "Other")
//
//    // UI
//    Box(modifier = modifier.fillMaxSize()) {
//        GoogleMap(
//            modifier = Modifier.fillMaxSize(),
//            cameraPositionState = cameraPositionState,
//            properties = MapProperties(isMyLocationEnabled = locationPermission.status.isGranted),
//            onMapClick = { latLng ->
//                markerPosition = latLng
//                showTypeDialog = true
//            }
//        ) {
//            markerPosition?.let {
//                Marker(
//                    state = MarkerState(position = it),
//                    title = markerType,
//                    icon = when(markerType) {
//                        "Home" -> BitmapDescriptorFactory.fromResource(R.drawable.ic_home)
//                        "Office" -> BitmapDescriptorFactory.fromResource(R.drawable.ic_office)
//                        else -> BitmapDescriptorFactory.fromResource(R.drawable.ic_other)
//                    }
//                )
//            }
//        }
//
//        // Search Bar
//        Surface(
//            tonalElevation = 3.dp,
//            shape = RoundedCornerShape(24.dp),
//            modifier = Modifier.padding(16.dp).fillMaxWidth().align(Alignment.TopCenter)
//        ) {
//            Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
//                OutlinedTextField(
//                    value = searchQuery,
//                    onValueChange = { searchQuery = it },
//                    placeholder = { Text("Search location...") },
//                    modifier = Modifier.weight(1f),
//                    singleLine = true,
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedContainerColor = Color.White,
//                        unfocusedContainerColor = Color.White
//                    )
//                )
//                Spacer(Modifier.width(8.dp))
//                IconButton(onClick = {
//                    scope.launch {
//                        runCatching {
//                            val geo = Geocoder(context, Locale.getDefault())
//                            val address = geo.getFromLocationName(searchQuery, 1)?.firstOrNull()
//                            address?.let {
//                                val latLng = LatLng(it.latitude, it.longitude)
//                                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
//                                markerPosition = latLng
//                                showTypeDialog = true
//                            }
//                        }.onFailure {
//                            Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }) {
//                    Icon(Icons.Default.LocationOn, contentDescription = "Search")
//                }
//            }
//        }
//
//        // Add marker at current location FAB
//        FloatingActionButton(
//            onClick = {
//                if (locationPermission.status.isGranted) {
//                    // In real code, get current location from FusedLocationProviderClient/Location APIs.
//                    // Here just move map to current camera
//                    markerPosition = cameraPositionState.position.target
//                    showTypeDialog = true
//                } else {
//                    locationPermission.launchPermissionRequest()
//                }
//            },
//            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
//        ) {
//            Icon(Icons.Default.AddLocation, "Add Marker at Location")
//        }
//
//        // Marker Type Dialog
//        if (showTypeDialog && markerPosition != null) {
//            AlertDialog(
//                onDismissRequest = { showTypeDialog = false },
//                title = { Text("Select Marker Type") },
//                text = {
//                    Column {
//                        markerTypes.forEach { type ->
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clickable {
//                                        markerType = type
//                                        showTypeDialog = false
//                                    }
//                                    .padding(vertical = 8.dp)
//                            ) {
//                                RadioButton(
//                                    selected = markerType == type,
//                                    onClick = {
//                                        markerType = type
//                                        showTypeDialog = false
//                                    }
//                                )
//                                Text(type, Modifier.padding(start = 8.dp))
//                            }
//                        }
//                    }
//                },
//                confirmButton = {
//                    TextButton(onClick = { showTypeDialog = false }) {
//                        Text("OK")
//                    }
//                }
//            )
//        }
//    }
//}
//
