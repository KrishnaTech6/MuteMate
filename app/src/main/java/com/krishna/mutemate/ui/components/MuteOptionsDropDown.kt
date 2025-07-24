package com.krishna.mutemate.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.krishna.mutemate.model.AllMuteOptions
import com.krishna.mutemate.utils.MuteSettingsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuteOptionsDropDown(
    context: Context = LocalContext.current,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier
        .padding(horizontal = 16.dp)
        .background(color = Color.White)
        .border(1.dp, Color.Black.copy(0.3f), RoundedCornerShape(16.dp))
    ) {
        val muteSettingsManager = remember { MuteSettingsManager(context) }
        val coroutineScope = rememberCoroutineScope()
        val options by muteSettingsManager.allMuteOptions.collectAsState(AllMuteOptions(isDnd = true))
        val titles = listOf("DND Mode", "Vibration Mode", "Custom Mode")
        var title by remember { mutableStateOf(titles[0]) }
        // State for expand/collapse
        var soundProfileExpanded by remember { mutableStateOf(false) }
        val soundCustomizationExpanded = remember { mutableStateOf(false) }

            // Sound Profile Settings Dropdown
            ExpandableCard(
                title = title,
                expanded = soundProfileExpanded,
                onExpandChanged = { soundProfileExpanded = it },
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                RowWithSwitch(
                    title = "DND Mode",
                    checked = options.isDnd,
                    onCheckedChange = {
                        coroutineScope.launch {
                            muteSettingsManager.saveAllSettings(options.copy(isDnd = it))
                        }
                        if(it){
                            title = titles[0]
                            soundProfileExpanded = false
                        }

                    }
                )
                RowWithSwitch(
                    title = "Vibration Mode",
                    checked = options.isVibrate,
                    enabled = !options.isDnd,
                    onCheckedChange = {
                        coroutineScope.launch {
                            muteSettingsManager.saveAllSettings(options.copy(isVibrate = it))
                        }

                        if(it){
                            title = titles[1]
                            soundProfileExpanded = false
                        }
                    }
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Sound Customization Dropdown
                ExpandableCard(
                    title = "Sound Customization",
                    expanded = soundCustomizationExpanded.value,
                    onExpandChanged = { soundCustomizationExpanded.value = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    if (options.isDnd || options.isVibrate) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(4.dp)
                        ) {
                            Text(
                                text = if (options.isDnd) "DND mode active - settings disabled"
                                else "Vibration mode active - settings disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    SoundToggle(
                        title = "Ringtone",
                        isChecked = options.muteType.muteRingtone,
                        enabled = !options.isDnd && !options.isVibrate
                    ) { isChecked ->
                        coroutineScope.launch {
                            muteSettingsManager.saveSetting(
                                MuteSettingsManager.MUTE_RINGTONE_KEY,
                                isChecked
                            )
                        }
                        title = titles[2]
                    }

                    SoundToggle(
                        title = "Notifications",
                        isChecked = options.muteType.muteNotifications,
                        enabled = !options.isDnd && !options.isVibrate
                    ) { isChecked ->
                        coroutineScope.launch {
                            muteSettingsManager.saveSetting(
                                MuteSettingsManager.MUTE_NOTIFICATIONS_KEY,
                                isChecked
                            )
                            title = titles[2]
                        }
                    }

                    SoundToggle(
                        title = "Alarms",
                        isChecked = options.muteType.muteAlarm,
                        enabled = !options.isDnd && !options.isVibrate
                    ) { isChecked ->
                        coroutineScope.launch {
                            muteSettingsManager.saveSetting(
                                MuteSettingsManager.MUTE_ALARM_KEY,
                                isChecked
                            )
                            title = titles[2]
                        }
                    }

                    SoundToggle(
                        title = "Media",
                        isChecked = options.muteType.muteMedia,
                        enabled = !options.isDnd && !options.isVibrate
                    ) { isChecked ->
                        coroutineScope.launch {
                            muteSettingsManager.saveSetting(
                                MuteSettingsManager.MUTE_MEDIA_KEY,
                                isChecked
                            )
                        }
                        title = titles[2]
                    }
                }
            }
    }
}

@Composable
fun ExpandableCard(
    title: String,
    expanded: Boolean,
    onExpandChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                androidx.compose.material3.IconButton(onClick = { onExpandChanged(!expanded) }) {
                    androidx.compose.material3.Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand"
                    )
                }
            }
            androidx.compose.animation.AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(4.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun RowWithSwitch(title: String, checked: Boolean, enabled: Boolean = true, modifier: Modifier = Modifier , onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = modifier
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
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
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
