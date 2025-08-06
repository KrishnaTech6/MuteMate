package com.krishna.mutemate.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krishna.mutemate.model.AllMuteOptions
import com.krishna.mutemate.model.LocationMute
import com.krishna.mutemate.model.MuteSchedule
import com.krishna.mutemate.ui.components.LabelDescription
import com.krishna.mutemate.ui.components.NoItemInList
import com.krishna.mutemate.utils.getTimeUntilStart
import com.krishna.mutemate.viewmodel.MapViewModel
import com.krishna.mutemate.viewmodel.MuteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun MuteListScreen(
    muteViewModel: MuteViewModel,
    mapViewModel: MapViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        val scheduleList by muteViewModel.allSchedules.collectAsState(initial = emptyList())
        val locationList by mapViewModel.allLocationMute.collectAsState(initial = emptyList())
        val schedules = scheduleList.sortedBy { getTimeUntilStart(it.startTime) }
        fun showToast(msg: String) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(msg)
            }
        }

        Box(Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
            Column {
                LabelDescription(title = "All Schedules")
                Spacer(Modifier.height(16.dp))
                if (schedules.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        NoItemInList()
                    }
                } else {
                    LazyColumn {
                        items(schedules) { schedule ->
                            ScheduleItem(
                                schedule,
                                getTimeUntilStart(schedule.startTime) <= 0,
                                muteViewModel
                            ) {
                                muteViewModel.deleteSchedule(it)
                                showToast("Schedule removed")
                            }
                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth()) {
            Column {
                LabelDescription(title = "All Mute Locations")
                Spacer(Modifier.height(16.dp))
                if (locationList.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                    ) {
                        NoItemInList("No location based mutes", Icons.Default.LocationOff)
                    }
                } else {
                    LazyColumn {
                        items(locationList) { muteLocation ->
                            LocationMuteItem(
                                locationMute = muteLocation,
                                isActive = true,
                                onEdit = {

                                },
                                onRemove = {
                                    mapViewModel.deleteLocationMute(
                                        context = context,
                                        locationMute = muteLocation
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleItem(
    schedule: MuteSchedule,
    isRunning: Boolean = false,
    viewModel: MuteViewModel,
    onRemove: (MuteSchedule) -> Unit
) {
    val formattedScheduleTime = remember(schedule) {
        viewModel.formatScheduleDuration(schedule.startTime, schedule.endTime)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                1.dp,
                if (!isRunning) Color.Black.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary,
                RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Schedule ${schedule.id}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    ScheduleText(schedule, viewModel)
                }
                val timeRemaining by viewModel.remainingTimeFlow(schedule.endTime).collectAsState(initial = getTimeUntilStart(schedule.startTime))
                var text = viewModel.formatTimeRemaining(timeRemaining, isEnd = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Time",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (formattedScheduleTime.isEmpty()) text else formattedScheduleTime,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        maxLines = 1
                    )
                }
            }
            MuteSummaryIcons(schedule.muteOptions, Modifier.weight(0.5f))
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = "Remove",
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onRemove(schedule) }
            )
        }
    }
}

@Composable
fun ScheduleText(schedule: MuteSchedule, viewModel: MuteViewModel) {
    val timeRemaining by viewModel.remainingTimeFlow(schedule.startTime).collectAsState(initial = getTimeUntilStart(schedule.startTime))
    var text = viewModel.formatTimeRemaining(timeRemaining, isEnd = false)

    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
    )
}


@Composable
fun LocationMuteItem(
    locationMute: LocationMute,
    isActive: Boolean = false,
    onEdit: (LocationMute) -> Unit,
    onRemove: (LocationMute) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                1.dp,
                if (!isActive) Color.Black.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary,
                RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Marker + Title
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = locationMute.markerType),
                    contentDescription = "Marker Type",
                    modifier = Modifier.size(28.dp),
                    tint = Color.Unspecified // Keep original marker color
                )

                Spacer(Modifier.width(8.dp))

                Column {
                    Text(
                        text = locationMute.title ?: "Unnamed Location",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Show mute options summary
                    Text(
                        text = buildMuteSummary(locationMute.muteOptions),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Edit button
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Location Mute",
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onEdit(locationMute) }
            )

            // Delete button
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove Location Mute",
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onRemove(locationMute) }
            )
        }
    }
}

@Composable
fun buildMuteSummary(options: AllMuteOptions): String {
    val list = mutableListOf<String>()
    if (options.isDnd) list.add("DND")
    else if (options.isVibrate) list.add("Vibrate")
    else if (options.isMute) list.add("Mute All")
    else {
        if (options.muteType.muteRingtone) list.add("Ringtone Off")
        if (options.muteType.muteMedia) list.add("Media Off")
        if (options.muteType.muteNotifications) list.add("Notifications Off")
        if (options.muteType.muteAlarm) list.add("Alarm Off")
    }

    return if (list.isEmpty()) "No mute options" else list.joinToString(", ")
}

@Composable
fun MuteSummaryIcons(options: AllMuteOptions, modifier: Modifier = Modifier) {
    val icons = mutableListOf<ImageVector>()

    if (options.isDnd) icons.add(Icons.Default.DoNotDisturbOn) // DND
    else if (options.isVibrate) icons.add(Icons.Default.Vibration) // Vibrate
    else if (options.isMute) icons.add(Icons.Default.VolumeOff) // Mute All
    else {
        if (options.muteType.muteRingtone) icons.add(Icons.Default.PhoneDisabled) // Ringtone Off
        if (options.muteType.muteMedia) icons.add(Icons.Default.MusicOff) // Media Off (custom icon if needed)
        if (options.muteType.muteNotifications) icons.add(Icons.Default.NotificationsOff) // Notifications Off
        if (options.muteType.muteAlarm) icons.add(Icons.Default.AlarmOff) // Alarm Off
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier, horizontalArrangement = Arrangement.Center) {
        if (icons.isEmpty()) {
            Icon(
                imageVector = Icons.Default.Block,
                contentDescription = "No mute options",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        } else {
            icons.forEach { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(18.dp)
                )
            }
        }
    }
}

