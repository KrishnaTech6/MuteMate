package com.example.mutemate

import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mutemate.model.MuteSchedule
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MuteScreen(viewModel: MuteViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    val startPickerDialog = remember { mutableStateOf(false) }
    val endPickerDialog = remember { mutableStateOf(false) }
    val selectedDuration = remember { mutableIntStateOf(0) }
    val isCustomTime = remember { mutableStateOf(false) }
    var customTimeSelected by remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val schedules by viewModel.allSchedules.collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "MuteMate", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(text = "One-Touch mute scheduler", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        if (!customTimeSelected) {
            Text(text = "Schedule mute for", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(10.dp))
            DurationSelection(
                selectedDuration.intValue,
                onDurationSelected = { selectedDuration.intValue = it },
                onNewDurationAdd = {5},
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "or select custom time", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(4.dp))
            Switch(
                checked = customTimeSelected,
                onCheckedChange = { customTimeSelected = it }
            )
        }

        if (customTimeSelected) {
            selectedDuration.intValue = 0 // reset duration when custom time is selected
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            // Start Date & Time Picker
            TimeSelector(
                label = "Start Time",
                time = startTime,
                onClick = { startPickerDialog.value = true })
            if (startPickerDialog.value) {
                TimePickerDialog(context, { _, hour, minute ->
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE , minute)
                    }

                    // Ensure selected time is not before current time
                    if (selectedCalendar.timeInMillis >= calendar.timeInMillis) {
                        startTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                    } else {
                        Toast.makeText(context, "Please select a future time", Toast.LENGTH_SHORT)
                            .show()
                    }
                }, currentHour, currentMinute+1, true).show()
                startPickerDialog.value = false
            }

            Spacer(modifier = Modifier.height(12.dp))

            // End Date & Time Picker
            TimeSelector(
                label = "End Time",
                time = endTime,
                onClick = { endPickerDialog.value = true })
            if (endPickerDialog.value) {
                TimePickerDialog(context, { _, hour, minute ->
                    if (startTime.isNotEmpty()) {
                        val selectedEndCalendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute+2)
                        }
                        val startCalendar = Calendar.getInstance()
                        val (startHour, startMinute) = startTime.split(":").map { it.toInt() }
                        startCalendar.set(Calendar.HOUR_OF_DAY, startHour)
                        startCalendar.set(Calendar.MINUTE, startMinute)

                        // Ensure end time is after start time
                        if (selectedEndCalendar.timeInMillis > startCalendar.timeInMillis) {
                            endTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                        } else {
                            Toast.makeText(
                                context,
                                "End time must be after start time",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else
                        Toast.makeText(
                            context,
                            "Please select start time first",
                            Toast.LENGTH_SHORT
                        ).show()
                }, currentHour, currentMinute+2, true)
                    .show()
                endPickerDialog.value = false
            }
        }


        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (!hasNotificationPolicyAccess(context)) {
                    showDialog.value = true
                } else if (selectedDuration.intValue == 0 && !customTimeSelected)
                    Toast.makeText(context, "Please select duration", Toast.LENGTH_SHORT).show()
                else if (endTime.isEmpty() && customTimeSelected)
                    Toast.makeText(context, "Please select start and end time", Toast.LENGTH_SHORT)
                        .show()
                else {
                    if (!customTimeSelected) {
                        val endCalendar = Calendar.getInstance().apply {
                            add(Calendar.MINUTE, selectedDuration.value)
                        }
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        endTime = sdf.format(endCalendar.time)
                    }
                    viewModel.addSchedule(MuteSchedule(startTime = startTime, endTime = endTime))
                    Toast.makeText(context, "Schedule added", Toast.LENGTH_SHORT).show()
                    //Reset Values
                    endTime = ""
                    startTime = ""
                    customTimeSelected = false
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Save Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text("Enable Do Not Disturb Access") },
                text = {
                    Text(
                        "To ensure uninterrupted quiet hours, please enable Do Not Disturb (DND) mode manually. " +
                                "We can only guide you to the settings, but you must turn it on yourself.",
                        textAlign = TextAlign.Justify
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            requestNotificationPolicyAccess(context)
                            showDialog.value = false
                        }
                    ) {
                        Text("Go to DND Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        ScheduleList(schedule = schedules) {
            viewModel.deleteSchedule(schedules[it])
            Toast.makeText(context, "Schedule deleted", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun TimeSelector(label: String, time: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text = if (time.isNotEmpty()) time else label)
    }
}

@Composable
fun DurationSelection(selectedDuration: Int, onDurationSelected: (Int) -> Unit, onNewDurationAdd: () -> Int) {
    var options by remember { mutableStateOf(listOf(1, 5, 10, 15, 30, 60)) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        options.forEach { duration ->
            OutlinedButton(
                onClick = { onDurationSelected(duration) },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    1.dp,
                    if (selectedDuration == duration) MaterialTheme.colorScheme.primary else Color.Gray
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedDuration == duration) MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.2f
                    ) else Color.Transparent,
                    contentColor = if (selectedDuration == duration) MaterialTheme.colorScheme.primary else Color.Gray
                )
            ) {
                Text(text = "$duration min")
            }
        }
//        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier
//            .padding(end = 8.dp)
//            .align(Alignment.CenterVertically)
//            .clickable {
//                val newDuration = onNewDurationAdd()
//                options = (options + newDuration)
//            }, tint = Color.Gray)
    }
}

fun hasNotificationPolicyAccess(context: Context): Boolean {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return notificationManager.isNotificationPolicyAccessGranted
}

fun requestNotificationPolicyAccess(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    context.startActivity(intent)
}
