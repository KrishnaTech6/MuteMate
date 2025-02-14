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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    index: Int ,
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
                text = schedule.startTime + " to " + schedule.endTime,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.clickable{
            //remove
            onRemove(index)
        })
    }
}