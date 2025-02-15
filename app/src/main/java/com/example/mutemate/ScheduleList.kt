package com.example.mutemate

import android.util.Log
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
import com.example.mutemate.utils.Constants
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
            Row {
                Text(
                    text = "Schedule ${index + 1}",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.width(8.dp))
                ScheduleText(schedule)
            }
            var timeRemaining by remember(schedule.endTime) { mutableStateOf(getTimeUntilStart(schedule.endTime)) }
            var text by remember(schedule.endTime) { mutableStateOf(formatTimeRemaining(timeRemaining, isEnd= true)) }

            LaunchedEffect(schedule.endTime) {
                while (timeRemaining > 0) {
                    text = formatTimeRemaining(timeRemaining, isEnd = true)
                    delay(if (timeRemaining > 2*60) 60 * 1000 else 1000) // Adjust update frequency
                    timeRemaining = getTimeUntilStart(schedule.endTime)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = "Time", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if(formattedScheduleTime.isEmpty()) text else formattedScheduleTime,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    maxLines = 1
                )
            }
        }
        Icon(Icons.Default.Remove, contentDescription = "Remove", modifier = Modifier
            .padding(4.dp)
            .clickable { onRemove(index) })
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
        text = "Running"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
    )
}

fun formatScheduleDuration(startTime: String, endTime: String): String {
    return try {
        if (startTime.isEmpty()) return ""
        val inputFormat = SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val startDate = inputFormat.parse(startTime) ?: ""
        val endDate = inputFormat.parse(endTime)?:""

        val startFormatted = outputFormat.format(startDate)
        val endFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(endDate)

        val isSameDay = SimpleDateFormat("dd MMM", Locale.getDefault()).format(startDate) ==
                SimpleDateFormat("dd MMM", Locale.getDefault()).format(endDate)

        return if (isSameDay) "$startFormatted – $endFormatted"
        else "$startFormatted – ${outputFormat.format(endDate)}"
    } catch (e: Exception) {
        Log.e("ScheduleDuration", "Error formatting schedule duration: ${e.message}")
        return ""
    }
}

fun formatTimeRemaining(timeRemaining: Int, isEnd: Boolean= false ): String {
    val endOrStart= if(!isEnd) "Starts" else "Ends"
    return when {
        timeRemaining >= 3600 -> "$endOrStart in ${timeRemaining / 3600}h ${timeRemaining % 3600 / 60}m".trimEnd()
        timeRemaining >= 120 -> "$endOrStart in ${timeRemaining / 60}m"
        timeRemaining >= 60 -> "$endOrStart in 1m"
        timeRemaining > 0 -> "$endOrStart in ${timeRemaining}s"
        else -> "Running"
    }
}

