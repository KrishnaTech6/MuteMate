package com.example.mutemate.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mutemate.utils.AccessibilityUtils
import com.example.mutemate.utils.MuteSettingsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilentModeSettingsScreen(onDismissRequest: () -> Unit, bottomSheetState: SheetState, context: Context = LocalContext.current, modifier: Modifier = Modifier) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState,
        modifier = modifier
    ) {
        val muteSettingsManager = remember { MuteSettingsManager(context) }
        val coroutineScope = rememberCoroutineScope()

        val isDnd by muteSettingsManager.isDnd.collectAsState(initial = true)
        val muteRingtone by muteSettingsManager.muteRingtone.collectAsState(initial = false)
        val isVibrationMode by muteSettingsManager.isVibrate.collectAsState(initial = false)
        val muteNotifications by muteSettingsManager.muteNotifications.collectAsState(initial = false)
        val muteAlarms by muteSettingsManager.muteAlarms.collectAsState(initial = false)
        val muteMedia by muteSettingsManager.muteMedia.collectAsState(initial = false)
        val isQuickMuteGestureEnabled by muteSettingsManager.isQuickMuteGestureEnabled.collectAsState(initial = false)
        val quickMuteDuration by muteSettingsManager.quickMuteDuration.collectAsState(initial = 30)
        var showAccessibilityDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            val isAccessibilityEnabled = AccessibilityUtils.isAccessibilityServiceEnabled(context)
            if (isAccessibilityEnabled) {
                muteSettingsManager.saveSetting(MuteSettingsManager.QUICK_MUTE_ENABLED, true)
            } else{
                muteSettingsManager.saveSetting(MuteSettingsManager.QUICK_MUTE_ENABLED, false)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Quick Mute Feature Card (USP)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Mute Gesture",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Quickly mute your device",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "💡 Triple-press the volume down button anywhere in the system to instantly mute your device!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Quick Mute",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Switch(
                            checked = isQuickMuteGestureEnabled,
                            onCheckedChange = { isChecked ->
                                coroutineScope.launch {
                                    if (!AccessibilityUtils.isAccessibilityServiceEnabled(context)){
                                        showAccessibilityDialog = true
                                    } else {
                                        Toast.makeText(context, "Quick Mute Gesture Enabled", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Mute Duration: $quickMuteDuration minute${if(quickMuteDuration > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Slider(
                        value = quickMuteDuration.toFloat(),
                        onValueChange = { newValue ->
                            val roundedValue = newValue.toInt()
                            coroutineScope.launch {
                                muteSettingsManager.updateQuickMuteDuration(roundedValue)
                            }
                        },
                        valueRange = 1f..120f,
                        steps = 23, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        enabled = isQuickMuteGestureEnabled
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "1 min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "2 hrs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Sound Profile Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Sound Settings Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "DND Mode",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = isDnd,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    muteSettingsManager.updateDndMode(it)
                                }
                            }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Vibration Mode",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = isVibrationMode,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    muteSettingsManager.updateVibrationMode(it)
                                }
                            },
                            enabled = !isDnd
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Sound Customization",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Sound Toggles Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (isDnd || isVibrationMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), 
                                    shape = RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = if (isDnd) "DND mode is active - individual sound settings disabled" 
                                       else "Vibration mode is active - individual sound settings disabled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    SoundToggle(
                        "Ringtone",
                        muteRingtone,
                        enabled = !isDnd && !isVibrationMode
                    ) { isChecked ->
                        coroutineScope.launch {
                            muteSettingsManager.saveSetting(
                                MuteSettingsManager.RINGTONE_KEY,
                                isChecked
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    SoundToggle(
                        "Notifications",
                        muteNotifications,
                        enabled = !isDnd && !isVibrationMode
                    ) { isChecked ->
                        coroutineScope.launch {
                            muteSettingsManager.saveSetting(
                                MuteSettingsManager.NOTIFICATIONS_KEY,
                                isChecked
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    SoundToggle(
                        "Alarms",
                        muteAlarms,
                        enabled = !isDnd && !isVibrationMode
                    ) { isChecked ->
                        coroutineScope.launch {
                            muteSettingsManager.saveSetting(
                                MuteSettingsManager.ALARMS_KEY,
                                isChecked
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    SoundToggle(
                        "Media",
                        muteMedia,
                        enabled = !isDnd && !isVibrationMode
                    ) { isChecked ->
                        coroutineScope.launch {
                            muteSettingsManager.saveSetting(
                                MuteSettingsManager.MEDIA_KEY,
                                isChecked
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Show accessibility service dialog if needed
        if (showAccessibilityDialog) {
            AlertDialog(
                onDismissRequest = { showAccessibilityDialog = false },
                title = { Text("Enable Accessibility Service") },
                text = { 
                    Column {
                        Text("To use the Quick Mute feature, MuteMate needs accessibility permissions.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("This allows the app to detect when you triple-press the volume down button to instantly mute your device.", 
                             style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showAccessibilityDialog = false
                            context.startActivity(AccessibilityUtils.getAccessibilitySettingsIntent())
                        }
                    ) {
                        Text("Enable Access")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showAccessibilityDialog = false }
                    ) {
                        Text("Later")
                    }
                }
            )
        }
    }
}

@Composable
fun SoundToggle(title: String, isChecked: Boolean, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface 
                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}