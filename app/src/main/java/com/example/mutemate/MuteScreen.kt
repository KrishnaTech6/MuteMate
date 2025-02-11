package com.example.mutemate

import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MuteScreen(viewModel: MuteViewModel, context: Context) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    val timePickerDialog = remember { mutableStateOf(false) }
    val selectedDuration = remember { mutableStateOf(0) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Set Mute Schedule", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { timePickerDialog.value = true }) {
            Text(text = if (startTime.isNotEmpty()) startTime else "Select Start Time")
        }

        if (timePickerDialog.value) {
            val timePicker = TimePickerDialog(context, { _, hour, minute ->
                startTime = String.format("%02d:%02d", hour, minute)
                timePickerDialog.value = false
            }, 0, 0, true)

            timePicker.show()
        }

        Text(text = "Select Duration")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(10, 20, 30, 60).forEach { duration ->
                Button(onClick = { selectedDuration.value = duration }) {
                    Text("${duration}min")
                }
            }
        }

        Button(onClick = {
            if (!hasNotificationPolicyAccess(context)) {
                requestNotificationPolicyAccess(context)
            } else {
                val endCalendar = Calendar.getInstance().apply {
                    add(Calendar.MINUTE, selectedDuration.value)
                }
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                endTime = sdf.format(endCalendar.time)
                viewModel.addSchedule(startTime, endTime)
            }
        }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Save Schedule")
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