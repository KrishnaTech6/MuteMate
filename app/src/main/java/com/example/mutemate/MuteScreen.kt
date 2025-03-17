package com.example.mutemate

import DateTimeSelector
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mutemate.model.MuteSchedule
import com.example.mutemate.utils.MuteHelper
import com.example.mutemate.utils.MuteSettingsManager
import com.example.mutemate.utils.SharedPrefUtils
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
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var startTime: Date? by remember { mutableStateOf(null) }
    var endTime: Date? by remember { mutableStateOf(null) }
    val selectedDuration = remember { mutableIntStateOf(0) }
    var customTimeSelected by remember { mutableStateOf(false) }
    val isDnd by MuteSettingsManager(context).isDnd.collectAsState(initial = true)
    val isVibrationMode by MuteSettingsManager(context).isVibrate.collectAsState(initial = true)
    val showDialog = remember { mutableStateOf(false) }
    val schedules by viewModel.allSchedules.collectAsState(initial = emptyList())
    val formattedScheduleTime by remember(schedules) {
        mutableStateOf(schedules.sortedBy {
            getTimeUntilStart(
                it.startTime
            )
        })
    }

    fun showToast(msg: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(msg)
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!customTimeSelected) {
            Text(text = "Schedule mute for", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(10.dp))
            DurationSelection(
                selectedDuration.intValue,
                onDurationSelected = { selectedDuration.intValue = it },
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "or select custom date and time",
                style = MaterialTheme.typography.bodyMedium
            )
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
                snackbarHostState = snackbarHostState,
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
                else if (endTime == null && customTimeSelected)
                    showToast("Please select start and end time")
                else {
                    if (!customTimeSelected) {
                        val endMillis =
                            System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(selectedDuration.intValue.toLong())
                        endTime = Date(endMillis)
                    }
                    viewModel.addSchedule(
                        MuteSchedule(
                            startTime = startTime,
                            endTime = endTime,
                            isDnd = isDnd,
                            isVibrationMode = isVibrationMode
                        )
                    )
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
        ) {
            Text("Set Schedule", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp))
        }
        if (showDialog.value) {
            ShowDndAlert(showDialog, context)
        }
        if (formattedScheduleTime.isEmpty()) {
            NoRunningSchedule(modifier = Modifier.padding(top = 100.dp))
        } else {
            ScheduleList(schedule = formattedScheduleTime) {
                viewModel.deleteSchedule(formattedScheduleTime[it])
                showToast("Schedule deleted")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Instant Actions",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        MuteOptionButtons()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview
fun MuteOptionButtons(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    val muteHelper = remember { MuteHelper(context) }

    val buttonConfigs = listOf(
        "DND" to Icons.Default.DoNotDisturb,
        "Mute" to Icons.Default.NotificationsOff,
        "Vibrate" to Icons.Default.Vibration
    )

    val selectedStates = remember { mutableStateMapOf<String, Boolean>() }

    // Load saved states
    LaunchedEffect(Unit) {
        val savedStates = SharedPrefUtils.getList(context, "mute_states")
        buttonConfigs.forEachIndexed { index, (title, _) ->
            selectedStates[title] = savedStates?.contains(index) == true
        }
    }

    fun saveState() {
        val activeIndexes = buttonConfigs.mapIndexedNotNull { index, (title, _) ->
            if (selectedStates[title] == true) index else null
        }
        SharedPrefUtils.saveList(context, activeIndexes,"mute_states")
    }


    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            buttonConfigs.forEach { (title, icon) ->
                ButtonMute(
                    icon = icon,
                    title = title,
                    isSelected = selectedStates[title] ?: false,
                    onItemClick = { isSelected ->
                        selectedStates[title] = !isSelected
                        saveState()
                        when (title) {
                            "DND"-> if (selectedStates[title] == true) muteHelper.dndModeOn() else muteHelper.normalMode()
                            "Mute" -> if (selectedStates[title] == true) muteHelper.mutePhone(true, true, true, true) else muteHelper.unmutePhone()
                            "Vibrate" -> if (selectedStates[title] == true) muteHelper.vibrateModePhone() else muteHelper.normalMode()
                        }
                    }
                )
            }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DurationSelection(selectedDuration: Int, onDurationSelected: (Int) -> Unit) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var options by remember {
        mutableStateOf(SharedPrefUtils.getList(context).takeIf { !it.isNullOrEmpty() } ?: listOf(
            1,
            5,
            10,
            15
        ))
    }
    var newDurationText by remember { mutableStateOf("") }
    LaunchedEffect(options) {
        SharedPrefUtils.saveList(context, options)
    }

    var showRemoveDialog by remember { mutableStateOf(false) }
    var durationToRemove by remember { mutableStateOf<Int?>(null) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        options.forEach { duration ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = if (selectedDuration == duration) MaterialTheme.colorScheme.primary else Color.Gray,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                        if (selectedDuration == duration) MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.2f
                        )
                        else Color.Transparent
                    )
                    .combinedClickable(
                        onClick = { onDurationSelected(duration) },
                        onLongClick = {
                            durationToRemove = duration
                            showRemoveDialog = true
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "$duration min",
                    color = if (selectedDuration == duration) MaterialTheme.colorScheme.primary else Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier
            .padding(end = 8.dp)
            .align(Alignment.CenterVertically)
            .clickable {
                showDialog = true
            }, tint = Color.Gray
        )
    }

    // Custom Duration Input Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Custom Duration") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newDurationText,
                        onValueChange = { newDurationText = it },
                        label = { Text("Enter duration (minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newDuration = newDurationText.toIntOrNull()
                        if (newDuration != null && newDuration > 0 && newDuration !in options) {
                            options = (options + newDuration).sorted()
                            newDurationText = ""
                            showDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove Duration?") },
            text = { Text("Are you sure you want to remove $durationToRemove min?") },
            confirmButton = {
                Button(
                    onClick = {
                        durationToRemove?.let {
                            options = options.filter { it != durationToRemove }
                            SharedPrefUtils.saveList(context, options)
                        }
                        showRemoveDialog = false
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
