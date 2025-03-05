package com.example.mutemate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
@Preview(showBackground = true)
fun SilentModeSettingsScreen(modifier: Modifier = Modifier) {
    var isDnd by remember { mutableStateOf(false) }

    var muteRingtone by remember { mutableStateOf(false) }
    var muteNotifications by remember { mutableStateOf(false) }
    var muteAlarms by remember { mutableStateOf(false) }
    var muteMedia by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(text = "Silent Mode Settings", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Mode Selection (DND or Custom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = {
                            isDnd= true
                        }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = isDnd,
                    onCheckedChange = {isDnd=!isDnd},
                )
                Text(text = "DND Mode", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 8.dp))
            }

        Spacer(modifier = Modifier.height(16.dp))

        // Sound Toggles (Disabled if DND Mode is selected)
        SoundToggle("Mute Ringtone", muteRingtone, enabled = !isDnd) { isChecked ->
            muteRingtone = isChecked
        }

        SoundToggle("Mute Notifications", muteNotifications, enabled = !isDnd) { isChecked ->
            muteNotifications = isChecked
        }

        SoundToggle("Mute Alarms", muteAlarms, enabled = !isDnd) { isChecked ->
            muteAlarms = isChecked
        }

        SoundToggle("Mute Media", muteMedia, enabled = !isDnd) { isChecked ->
            muteMedia = isChecked
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
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}
