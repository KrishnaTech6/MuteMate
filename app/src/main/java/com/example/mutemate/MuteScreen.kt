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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MuteScreen(viewModel: MuteViewModel, context: Context) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    val startPickerDialog = remember { mutableStateOf(false) }
    val endPickerDialog = remember { mutableStateOf(false) }
    val selectedDuration = remember { mutableIntStateOf(0) }
    var customTimeSelected by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "MuteMate", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(text = "One-Touch mute scheduler", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        if (!customTimeSelected) {
            Text(text = "Schedule mute for", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(10.dp))
            DurationSelection(selectedDuration.intValue) { duration ->
                selectedDuration.intValue = duration
            }
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
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            // Start Date & Time Picker
            TimeSelector(label = "Start Time", time = startTime, onClick = { startPickerDialog.value = true })
            if (startPickerDialog.value) {
                TimePickerDialog(context, { _, hour, minute ->
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                    }

                    // Ensure selected time is not before current time
                    if (selectedCalendar.timeInMillis >= calendar.timeInMillis) {
                        startTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                    } else {
                        Toast.makeText(context, "Please select a future time", Toast.LENGTH_SHORT).show()
                    }
                    startPickerDialog.value = false
                }, currentHour, currentMinute, true).show()
            }

            Spacer(modifier = Modifier.height(12.dp))

            // End Date & Time Picker
            TimeSelector(label = "End Time", time = endTime, onClick = { endPickerDialog.value = true })
            if (endPickerDialog.value) {
                TimePickerDialog(context, { _, hour, minute ->
                    val selectedEndCalendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                    }
                    val startCalendar = Calendar.getInstance()
                    val (startHour, startMinute) = startTime.split(":").map { it.toInt() }
                    startCalendar.set(Calendar.HOUR_OF_DAY, startHour)
                    startCalendar.set(Calendar.MINUTE, startMinute)

                    // Ensure end time is after start time
                    if (selectedEndCalendar.timeInMillis > startCalendar.timeInMillis) {
                        endTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                    } else {
                        Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
                    }
                    endPickerDialog.value = false
                }, currentHour, currentMinute, true).show()
            }
        }


        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = {
                if (!hasNotificationPolicyAccess(context)) {
                    requestNotificationPolicyAccess(context)
                } else {
                    if (!customTimeSelected) {
                        val endCalendar = Calendar.getInstance().apply {
                            add(Calendar.MINUTE, selectedDuration.value)
                        }
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        endTime = sdf.format(endCalendar.time)
                    }
                    viewModel.addSchedule(startTime, endTime)
                    Toast.makeText(context, "Schedule added", Toast.LENGTH_SHORT).show()
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
fun DurationSelection(selectedDuration: Int, onDurationSelected: (Int) -> Unit) {
    val options = listOf(1, 5, 10, 15, 30, 60)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
        options.forEach { duration ->
            OutlinedButton(
                onClick = { onDurationSelected(duration) },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (selectedDuration == duration) MaterialTheme.colorScheme.primary else Color.Gray),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedDuration == duration) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                    contentColor = if (selectedDuration == duration) MaterialTheme.colorScheme.primary else Color.Gray
                )
            ) {
                Text(text = "$duration min")
            }
        }
    }
}

fun hasNotificationPolicyAccess(context: Context): Boolean {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return notificationManager.isNotificationPolicyAccessGranted
}

fun requestNotificationPolicyAccess(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    context.startActivity(intent)
}
