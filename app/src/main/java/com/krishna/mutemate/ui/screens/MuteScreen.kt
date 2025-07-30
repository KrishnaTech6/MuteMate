@file:OptIn(ExperimentalMaterial3Api::class)

package com.krishna.mutemate.ui.screens

import DateTimeSelector
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.krishna.mutemate.model.AllMuteOptions
import com.krishna.mutemate.model.MuteSchedule
import com.krishna.mutemate.ui.components.ButtonMute
import com.krishna.mutemate.ui.components.MuteOptionsDropDown
import com.krishna.mutemate.ui.features.QuickMuteGesture
import com.krishna.mutemate.utils.AccessibilityUtils
import com.krishna.mutemate.utils.MuteHelper
import com.krishna.mutemate.utils.MuteSettingsManager
import com.krishna.mutemate.utils.SharedPrefUtils
import com.krishna.mutemate.utils.checkExactAlarmPermission
import com.krishna.mutemate.utils.getTimeUntilStart
import com.krishna.mutemate.utils.hasNotificationPolicyAccess
import com.krishna.mutemate.utils.isBatteryLow
import com.krishna.mutemate.utils.requestNotificationPolicyAccess
import com.krishna.mutemate.utils.sendUserToExactAlarmSettings
import com.krishna.mutemate.viewmodel.MuteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date
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
    navController: NavController,
    viewModel: MuteViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var startTime: Date? by remember { mutableStateOf(null) }
    var endTime: Date? by remember { mutableStateOf(null) }
    val selectedDuration = remember { mutableIntStateOf(0) }
    var customTimeSelected by remember { mutableStateOf(false) }
    val muteSettingsManager = remember { MuteSettingsManager(context) }
    val options by muteSettingsManager.allMuteOptions.collectAsState(AllMuteOptions(isDnd = true))
    val showDialog = remember { mutableStateOf(false) }
    val showExactAlarmDialog = remember { mutableStateOf(checkExactAlarmPermission(context)) }
    // need to ask permissions after api 31

    val scheduleList by viewModel.allSchedules.collectAsState(emptyList())

    if (showDialog.value) {
        ShowDndAlert(showDialog, context)
    }

    if(showExactAlarmDialog.value){
        ShowExactAlarmAlert(showExactAlarmDialog, context)
    }

    fun showToast(msg: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(msg)
        }
    }
    // accessibility services
    LaunchedEffect(Unit) {
        val isAccessibilityEnabled = AccessibilityUtils.isAccessibilityServiceEnabled(context)
        if (isAccessibilityEnabled) {
            muteSettingsManager.saveSetting(MuteSettingsManager.QUICK_MUTE_ENABLED, true)
        } else {
            muteSettingsManager.saveSetting(MuteSettingsManager.QUICK_MUTE_ENABLED, false)
        }
    }

    Column(modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        if (!scheduleList.isEmpty() && scheduleList.find { getTimeUntilStart(startTime) <= 0 } != null) {
            Box(Modifier.padding(horizontal = 8.dp)) {
                ScheduleItem(schedule = scheduleList.find { getTimeUntilStart(startTime) <= 0 }!!, true, viewModel) {
                    viewModel.deleteSchedule(it)
                    showToast("Schedule removed")
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
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
            MuteOptionsDropDown(context,Modifier.padding(16.dp))
            ScheduleButton(
                context = context,
                customTimeSelected = customTimeSelected,
                selectedDuration = selectedDuration.intValue,
                startTime = startTime,
                endTime = endTime,
                isDnd = options.isDnd,
                isVibrationMode = options.isVibrate,
                onScheduleAdd = { schedule ->
                    viewModel.addSchedule(schedule)
                    showToast("Schedule added")
                    endTime = null
                    startTime = null
                },
                onShowDialog = { showDialog.value = true },
                onShowToast = { showToast(it) }
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Quick Mute Feature Card (USP)
            QuickMuteGesture(coroutineScope, context, muteSettingsManager)
            Spacer(modifier = Modifier.height(10.dp))
//        // Fixed bottom bar
//        Surface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 10.dp),
//            color = MaterialTheme.colorScheme.surfaceContainer,
//            tonalElevation = 12.dp,
//            shadowElevation = 6.dp
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(18.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                InstantActionsSection(showDndDialog = {showDialog.value=true})
//            }
//        }
        }
    }
}

@Composable
fun ShowExactAlarmAlert(showDialog: MutableState<Boolean>, context: Context) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text("Allow Exact Alarms") },
        text = {
            Text(
                "This app needs permission to set alarms that go off exactly on time, " +
                        "even when your phone is in Do Not Disturb mode or idle. " +
                        "Without this permission, scheduled unmute may be delayed.",
                textAlign = TextAlign.Justify
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    sendUserToExactAlarmSettings(context)
                    showDialog.value = false
                }
            ) {
                Text("Go to Settings")
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Schedule Section
        if (!customTimeSelected) {
            QuickScheduleSelector(
                onCustomTimeSelectedChange,
                selectedDuration,
                onDurationSelected
            )
        } else {
            // Custom Time Section
            CustomTimeSelector(
                onCustomTimeSelectedChange,
                coroutineScope,
                snackbarHostState,
                startTime,
                onStartTimeSelected,
                endTime,
                onEndTimeSelected
            )
        }
    }
}

@Composable
private fun QuickScheduleSelector(
    onCustomTimeSelectedChange: (Boolean) -> Unit,
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
}

@Composable
private fun CustomTimeSelector(
    onCustomTimeSelectedChange: (Boolean) -> Unit,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    startTime: Date?,
    onStartTimeSelected: (Date?) -> Unit,
    endTime: Date?,
    onEndTimeSelected: (Date?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.98f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                Date(
                                    System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(
                                        selectedDuration.toLong()
                                    )
                                )
                            } else endTime

                            val finalStartTime = if(!customTimeSelected) Date() else startTime

                            onScheduleAdd(
                                MuteSchedule(
                                    startTime = finalStartTime,
                                    endTime = finalEndTime,
                                    muteOptions = AllMuteOptions(isDnd, isVibrationMode)
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
private fun InstantActionsSection(showDndDialog: () -> Unit) {
    Text(
        text = "Instant Actions",
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = "Choose how your phone should be silenced instantly. Only one option active at a time.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Spacer(Modifier.height(4.dp))
    MuteOptionButtons(showDndDialog = showDndDialog)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview
fun MuteOptionButtons(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    showDndDialog: () -> Unit = {}
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
                                if (!hasNotificationPolicyAccess(context)) {
                                    showDndDialog()
                                } else muteHelper.dndModeOn()
                            }

                            "Mute" -> {
                                muteHelper.mutePhone(true, true, true, true)
                            }

                            "Vibrate" -> {
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = "Choose or add how long you want your phone to be muted.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
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
                            text = if(duration==1) "minute" else "minutes",
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
}
