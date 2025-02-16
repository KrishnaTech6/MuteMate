package com.example.mutemate

import DateTimeSelector
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import com.example.mutemate.utils.getTimeUntilStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun MuteScreen(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    viewModel: MuteViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var startTime: Date? by remember { mutableStateOf(null) }
    var endTime: Date? by remember { mutableStateOf(null) }
    val selectedDuration = remember { mutableIntStateOf(0) }
    var customTimeSelected by remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val schedules by viewModel.allSchedules.collectAsState(initial = emptyList())
    val formattedScheduleTime by remember(schedules) {mutableStateOf(schedules.sortedBy { getTimeUntilStart(it.startTime) })}

    fun showToast(msg: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(msg)
        }
    }

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
            DateTimeSelector(
                coroutineScope,
                snackbarHostState,
                label = "Start Date and Time",
                dateTime = startTime,
                onDateTimeSelected = { startTime = it }
            )
            Spacer(modifier = Modifier.height(10.dp))
            DateTimeSelector(
                coroutineScope = coroutineScope,
                snackbarHostState= snackbarHostState,
                label = "End Date and Time",
                dateTime = endTime,
                minDateTime = startTime
            ) { endTime = it }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (!hasNotificationPolicyAccess(context)) {
                    showDialog.value = true
                } else if (selectedDuration.intValue == 0 && !customTimeSelected)
                    showToast("Please select duration")
                else if (endTime==null && customTimeSelected)
                    showToast("Please select start and end time")
                else {
                    if (!customTimeSelected) {
                        val endMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(selectedDuration.intValue.toLong())
                        endTime = Date(endMillis)
                    }
                    viewModel.addSchedule(MuteSchedule(startTime = startTime, endTime = endTime))
                    showToast("Schedule added")
                    // Reset Values
                    endTime = null
                    startTime = null
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Set Mute Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        if (showDialog.value) {
            ShowDndAlert(showDialog, context)
        }
        ScheduleList(schedule = formattedScheduleTime) {
            viewModel.deleteSchedule(formattedScheduleTime[it])
            showToast("Schedule deleted")
        }
    }
}

@Composable
fun ShowDndAlert(
    showDialog: MutableState<Boolean>,
    context: Context,
) {
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
