package com.example.mutemate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


val list = listOf<  MuteSchedule>(
    MuteSchedule(startTime = "14:25", endTime = "12:34"),
    MuteSchedule(startTime = "14:25", endTime = "12:34"),
    MuteSchedule(startTime = "14:25", endTime = "12:34")
)

@Composable
@Preview(showBackground = true)
fun ScheduleList(schedule: List<MuteSchedule> = list, modifier: Modifier = Modifier) {
    LazyColumn {
        items(schedule.size) { index ->
            val schedule = schedule[index]
            ScheduleItem(schedule)
        }

    }

}

@Composable
@Preview(showBackground = true)
fun ScheduleItem(schedule: MuteSchedule = MuteSchedule(startTime = "14:25", endTime = "12:34")) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            text = "Schedule 1",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = schedule.endTime + " to " + schedule.endTime,
            style = MaterialTheme.typography.bodySmall
        )
    }
}