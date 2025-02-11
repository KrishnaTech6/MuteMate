package com.example.mutemate

import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MuteScreen(viewModel: MuteViewModel, context: Context) {
    var startTime by remember { mutableStateOf(getCurrentTime()) }
    var endTime by remember { mutableStateOf("") }
    val timePickerDialog = remember { mutableStateOf(false) }
    val selectedDuration = remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Set Mute Schedule", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { timePickerDialog.value = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = if (startTime!=getCurrentTime()) startTime else "Select Start Time")
        }

        if (timePickerDialog.value) {
            val timePicker = TimePickerDialog(context, { _, hour, minute ->
                startTime = String.format("%02d:%02d", hour, minute)
                timePickerDialog.value = false
            }, 0, 0, true)
            timePicker.setOnDismissListener {
                timePickerDialog.value = false
            }
            timePicker.show()
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Select Duration", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf(1, 5, 15, 30, 60).forEach { duration ->
                OutlinedButton(
                    onClick = { selectedDuration.value = duration },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("${duration} min")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
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
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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

fun getCurrentTime(): String {
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(calendar.time)
}