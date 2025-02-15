package com.example.mutemate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mutemate.model.MuteSchedule
import com.example.mutemate.utils.convertToAmPm
import com.example.mutemate.utils.getTimeUntilStart
import kotlinx.coroutines.delay

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
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Schedule ${index + 1}",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "${convertToAmPm(schedule.startTime)} to ${convertToAmPm(schedule.endTime)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        ScheduleText(schedule)
        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.clickable{
            //remove
            onRemove(index)
        })
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

fun formatTimeRemaining(timeRemaining: Int): String {
    return when {
        timeRemaining > 60 * 60 -> "Starts in ${timeRemaining / (60 * 60)}hr ${(timeRemaining / 60) % 60}min"
        timeRemaining > 2*60 -> "Starts in ${timeRemaining / 60} min"
        timeRemaining > 0 -> "Starts in ${timeRemaining / 60} min ${timeRemaining % 60} sec"
        else -> "Running"
    }
}
