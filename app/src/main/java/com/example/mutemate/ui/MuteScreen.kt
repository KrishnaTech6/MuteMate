package com.example.mutemate.ui

import DateTimeSelector
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mutemate.model.MuteSchedule
import com.example.mutemate.utils.MuteHelper
import com.example.mutemate.utils.MuteSettingsManager
import com.example.mutemate.utils.SharedPrefUtils
import com.example.mutemate.utils.getTimeUntilStart
import com.example.mutemate.utils.isBatteryLow
import com.example.mutemate.viewmodel.MuteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Main screen for managing mute schedules and instant actions
 * @param snackbarHostState State for showing snackbar messages
 * @param coroutineScope Coroutine scope for async operations
 * @param viewModel ViewModel for managing mute schedules
 * @param modifier Modifier for the root composable
 */
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
        mutableStateOf(schedules.sortedBy { getTimeUntilStart(it.startTime) })
    }

    fun showToast(msg: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(msg)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScheduleSection(
                customTimeSelected = customTimeSelected,
                selectedDuration = selectedDuration.intValue,
                startTime = startTime,
                endTime = endTime,
                onCustomTimeSelectedChange = { customTimeSelected = it },
                onDurationSelected = { selectedDuration.intValue = it },
                onStartTimeSelected = { startTime = it },
                onEndTimeSelected = { endTime = it },
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState
            )

            Spacer(modifier = Modifier.height(20.dp))

            ScheduleButton(
                context = context,
                customTimeSelected = customTimeSelected,
                selectedDuration = selectedDuration.intValue,
                startTime = startTime,
                endTime = endTime,
                isDnd = isDnd,
                isVibrationMode = isVibrationMode,
                onScheduleAdd = { schedule ->
                    viewModel.addSchedule(schedule)
                    showToast("Schedule added")
                    endTime = null
                    startTime = null
                },
                onShowDialog = { showDialog.value = true },
                onShowToast = { showToast(it) }
            )

            if (showDialog.value) {
                ShowDndAlert(showDialog, context)
            }

            if (formattedScheduleTime.isEmpty()) {
                NoRunningSchedule(modifier = Modifier.height(160.dp))
            } else {
                ScheduleList(
                    schedule = formattedScheduleTime,
                    onRemove = { index ->
                        viewModel.deleteSchedule(formattedScheduleTime[index])
                        showToast("Schedule deleted")
                    }
                )
            }
        }

        // Fixed bottom bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InstantActionsSection()
            }
        }
    }
}

@Composable
private fun ScheduleSection(
    customTimeSelected: Boolean,
    selectedDuration: Int,
    startTime: Date?,
    endTime: Date?,
    onCustomTimeSelectedChange: (Boolean) -> Unit,
    onDurationSelected: (Int) -> Unit,
    onStartTimeSelected: (Date?) -> Unit,
    onEndTimeSelected: (Date?) -> Unit,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Schedule Section
        if (!customTimeSelected) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Quick Schedule",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Select a preset duration",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onCustomTimeSelectedChange(true) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Switch to custom time",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                DurationSelection(
                    selectedDuration = selectedDuration,
                    onDurationSelected = onDurationSelected,
                )
            }
        } else {
            // Custom Time Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Custom Schedule",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Set your own start and end time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onCustomTimeSelectedChange(false) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Switch to quick schedule",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Start Time Selection
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Start Time",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DateTimeSelector(
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                            label = "Select start time",
                            dateTime = startTime,
                            onDateTimeSelected = onStartTimeSelected
                        )
                    }
                }

                // End Time Selection
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "End Time",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DateTimeSelector(
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                            label = "Select end time",
                            dateTime = endTime,
                            minDateTime = startTime,
                            onDateTimeSelected = onEndTimeSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleButton(
    context: Context,
    customTimeSelected: Boolean,
    selectedDuration: Int,
    startTime: Date?,
    endTime: Date?,
    isDnd: Boolean,
    isVibrationMode: Boolean,
    onScheduleAdd: (MuteSchedule) -> Unit,
    onShowDialog: () -> Unit,
    onShowToast: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Schedule Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Schedule Summary",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (customTimeSelected) {
                            "Custom time schedule"
                        } else {
                            "$selectedDuration minutes"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Schedule",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Settings Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // DND Setting
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DoNotDisturb,
                        contentDescription = "DND",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (isDnd) "DND On" else "DND Off",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                // Vibration Setting
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Vibration",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (isVibrationMode) "Vibrate On" else "Vibrate Off",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Action Button
            Button(
                onClick = {
                    when {
                        !hasNotificationPolicyAccess(context) -> onShowDialog()
                        isBatteryLow(context) -> onShowToast("Battery is low, can't schedule task")
                        (selectedDuration == 0 && !customTimeSelected) -> onShowToast("Please select duration")
                        (endTime == null && customTimeSelected) -> onShowToast("Please select start and end time")
                        else -> {
                            val finalEndTime = if (!customTimeSelected) {
                                Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(selectedDuration.toLong()))
                            } else endTime

                            onScheduleAdd(
                                MuteSchedule(
                                    startTime = startTime,
                                    endTime = finalEndTime,
                                    isDnd = isDnd,
                                    isVibrationMode = isVibrationMode
                                )
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Schedule",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Set Schedule",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun InstantActionsSection() {
    Text(
        text = "Instant Actions",
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.height(8.dp))
    MuteOptionButtons()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview
fun MuteOptionButtons(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
) {
    val muteHelper = remember { MuteHelper(context) }

    val buttonConfigs = listOf(
        "DND" to Icons.Default.DoNotDisturb,
        "Mute" to Icons.Default.NotificationsOff,
        "Vibrate" to Icons.Default.Vibration
    )

    val selectedState = remember { mutableStateOf<String?>(null) } // Only one selection

    // Load saved state
    LaunchedEffect(Unit) {
        val savedState = SharedPrefUtils.getString(context, "selected_mute_option")
        selectedState.value = savedState
    }

    fun saveState(selectedOption: String?) {
        SharedPrefUtils.saveString(context, selectedOption ?: "", "selected_mute_option")
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            buttonConfigs.forEach { (title, icon) ->
                ButtonMute(
                    icon = icon,
                    title = title,
                    isSelected = selectedState.value == title, // Check if this button is selected
                    onItemClick = {
                        val newState =
                            if (selectedState.value == title) null else title // Toggle logic
                        selectedState.value = newState
                        saveState(newState)

                        when (newState) {
                            "DND" -> {
                                muteHelper.normalMode()
                                muteHelper.dndModeOn()
                            }

                            "Mute" -> {
                                muteHelper.normalMode()
                                muteHelper.mutePhone(true, true, true, true)
                            }

                            "Vibrate" -> {
                                muteHelper.normalMode()
                                muteHelper.vibrateModePhone()
                            }

                            null -> muteHelper.normalMode() // Deselect all → Normal mode
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
            10
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
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
    ) {
        options.forEach { duration ->
            Card(
                modifier = Modifier
                    .width(100.dp)
                    .combinedClickable(
                        onClick = { onDurationSelected(duration) },
                        onLongClick = {
                            durationToRemove = duration
                            showRemoveDialog = true
                        }
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedDuration == duration) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$duration",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (selectedDuration == duration)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "minutes",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedDuration == duration)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Add Custom Duration Button
        Card(
            modifier = Modifier
                .width(100.dp)
                .clickable { showDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = "Add custom duration",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Custom",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }

    // Custom Duration Input Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { 
                Text(
                    "Add Custom Duration",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newDurationText,
                        onValueChange = { newDurationText = it },
                        label = { Text("Enter duration (minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
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
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { 
                Text(
                    "Remove Duration?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = { 
                Text(
                    "Are you sure you want to remove $durationToRemove min?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        durationToRemove?.let {
                            options = options.filter { it != durationToRemove }
                            SharedPrefUtils.saveList(context, options)
                        }
                        showRemoveDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
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
