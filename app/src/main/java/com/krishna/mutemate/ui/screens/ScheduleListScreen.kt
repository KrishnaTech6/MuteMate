package com.krishna.mutemate.ui.screens

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krishna.mutemate.model.MuteSchedule
import com.krishna.mutemate.ui.components.NoRunningSchedule
import com.krishna.mutemate.utils.formatTimeRemaining
import com.krishna.mutemate.utils.getTimeUntilStart
import com.krishna.mutemate.viewmodel.MuteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ScheduleListScreen(
    viewModel: MuteViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val scheduleList by viewModel.allSchedules.collectAsState(initial = emptyList())
        val schedules = scheduleList.sortedBy { getTimeUntilStart(it.startTime) }
        fun showToast(msg: String) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(msg)
            }
        }
        if(schedules.isEmpty()){
            NoRunningSchedule()
        }else {
            LazyColumn {
                items(schedules) { schedule ->
                    ScheduleItem(schedule, getTimeUntilStart(schedule.startTime) == 0) {
                        viewModel.deleteSchedule(it)
                        showToast("Schedule removed")
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
    onRemove: (MuteSchedule) -> Unit
) {
    val formattedScheduleTime = remember(schedule) {
        formatScheduleDuration(schedule.startTime, schedule.endTime)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, if(!isRunning) Color.Black.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary , RoundedCornerShape(12.dp))
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
                    ScheduleText(schedule)
                }
                var timeRemaining by remember(schedule.endTime) {
                    mutableStateOf(
                        getTimeUntilStart(
                            schedule.endTime
                        )
                    )
                }
                var text by remember(schedule.endTime) {
                    mutableStateOf(
                        formatTimeRemaining(
                            timeRemaining,
                            isEnd = true
                        )
                    )
                }

                LaunchedEffect(schedule.endTime) {
                    while (timeRemaining > 0) {
                        text = formatTimeRemaining(timeRemaining, isEnd = true)
                        delay(if (timeRemaining > 2 * 60) 60 * 1000 else 1000) // Adjust update frequency
                        timeRemaining = getTimeUntilStart(schedule.endTime)
                    }
                }
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
fun ScheduleText(schedule: MuteSchedule) {
    var timeRemaining by remember(schedule.startTime) { mutableStateOf(getTimeUntilStart(schedule.startTime)) }
    var text by remember(schedule.startTime) { mutableStateOf(formatTimeRemaining(timeRemaining)) }

    LaunchedEffect(schedule.startTime) {
        while (timeRemaining > 0) {
            text = formatTimeRemaining(timeRemaining)
            delay(if (timeRemaining > 2 * 60) 60 * 1000 else 1000) // Adjust update frequency
            timeRemaining = getTimeUntilStart(schedule.startTime)
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
    )
}

fun formatScheduleDuration(startTime: Date?, endTime: Date?): String {
    return try {
        if (startTime == null) return "Ends at ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(endTime)}"

        val outputFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

        val startFormatted = outputFormat.format(startTime)
        val endFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(endTime!!)

        val isSameDay = SimpleDateFormat("dd MMM", Locale.getDefault()).format(startTime) ==
                SimpleDateFormat("dd MMM", Locale.getDefault()).format(endTime)

        return if (isSameDay) "$startFormatted – $endFormatted"
        else "$startFormatted – ${outputFormat.format(endTime)}"
    } catch (e: Exception) {
        Log.e("ScheduleDuration", "Error formatting schedule duration: ${e.message}")
        return ""
    }
}

