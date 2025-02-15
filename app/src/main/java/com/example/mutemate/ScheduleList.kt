package com.example.mutemate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mutemate.model.MuteSchedule
import com.example.mutemate.utils.getTimeUntilStart
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ScheduleList(schedule: List<MuteSchedule>, onRemove: (Int) -> Unit) {
    LazyColumn {
        items(schedule.size) { index ->
            val schedule = schedule[index]
            ScheduleItem(index, schedule){
                onRemove(index)
            }
        }
    }
}

@Composable
fun ScheduleItem(
    index: Int,
    schedule: MuteSchedule,
    onRemove: (Int) -> Unit
) {
    val formattedScheduleTime = remember(schedule.startTime, schedule.endTime) {
        formatScheduleDuration(schedule.startTime, schedule.endTime)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Schedule ${index + 1}",
                style = MaterialTheme.typography.titleSmall
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = "Time", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formattedScheduleTime,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    maxLines = 1
                )
            }
        }
        ScheduleText(schedule)
        Icon(Icons.Default.Remove, contentDescription = "Remove", modifier = Modifier.padding(4.dp).clickable { onRemove(index) })
    }
}

@Composable
fun ScheduleText(schedule: MuteSchedule) {
    var timeRemaining by remember(schedule.startTime) { mutableStateOf(getTimeUntilStart(schedule.startTime)) }
    var text by remember(schedule.startTime) { mutableStateOf(formatTimeRemaining(timeRemaining)) }

    LaunchedEffect(schedule.startTime) {
        while (timeRemaining > 0) {
            text = formatTimeRemaining(timeRemaining)
            delay(if (timeRemaining > 2*60) 60 * 1000 else 1000) // Adjust update frequency
            timeRemaining = getTimeUntilStart(schedule.startTime)
        }
        text = "Running"
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
    )
}

fun formatScheduleDuration(startTime: String, endTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

        val startDate = inputFormat.parse(startTime)
        val endDate = inputFormat.parse(endTime)

        if (startDate == null || endDate == null) return "Ongoing"

        val startFormatted = outputFormat.format(startDate)
        val endFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(endDate)

        val isSameDay = SimpleDateFormat("dd MMM", Locale.getDefault()).format(startDate) ==
                SimpleDateFormat("dd MMM", Locale.getDefault()).format(endDate)

        return if (isSameDay) "$startFormatted – $endFormatted"
        else "$startFormatted – ${outputFormat.format(endDate)}"
    } catch (e: Exception) {
        "Ongoing"
    }
}

fun formatTimeRemaining(timeRemaining: Int): String {
    return when {
        timeRemaining > 60*60 -> "Starts in ${timeRemaining / (60 * 60)}hr ${(timeRemaining / 60) % 60}min"
        timeRemaining > 2*60 -> "Starts in ${timeRemaining / 60} min"
        timeRemaining > 0 -> "Starts in ${timeRemaining / 60} min ${timeRemaining % 60} sec"
        else -> "Running"
    }
}
