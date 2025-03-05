package com.example.mutemate

import android.content.Context
import android.util.Log
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mutemate.utils.MuteSettingsManager
import kotlinx.coroutines.launch


@Composable
@Preview(showBackground = true)
fun SilentModeSettingsScreen(context: Context = LocalContext.current, modifier: Modifier = Modifier) {
    val muteSettingsManager = remember { MuteSettingsManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val isDnd by muteSettingsManager.isDnd.collectAsState(initial = true)
    val muteRingtone by muteSettingsManager.muteRingtone.collectAsState(initial = true)
    val muteNotifications by muteSettingsManager.muteNotifications.collectAsState(initial = true)
    val muteAlarms by muteSettingsManager.muteAlarms.collectAsState(initial = true)
    val muteMedia by muteSettingsManager.muteMedia.collectAsState(initial = true)

    Log.d("SilentModeSettingsScreen", "isDnd: $isDnd, muteRingtone: $muteRingtone, muteNotifications: $muteNotifications, muteAlarms: $muteAlarms, muteMedia: $muteMedia")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(text = "Silent Mode Settings", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    coroutineScope.launch {
                        muteSettingsManager.updateDndMode(!isDnd)
                    }
                }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isDnd,
                onCheckedChange = {
                    coroutineScope.launch {
                        muteSettingsManager.updateDndMode(it)
                    }
                }
            )
            Text(text = "DND Mode", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        SoundToggle("Mute Ringtone", muteRingtone, enabled = !isDnd) { isChecked ->
            coroutineScope.launch { muteSettingsManager.saveSetting(MuteSettingsManager.RINGTONE_KEY, isChecked) }
        }

        SoundToggle("Mute Notifications", muteNotifications, enabled = !isDnd) { isChecked ->
            coroutineScope.launch { muteSettingsManager.saveSetting(MuteSettingsManager.NOTIFICATIONS_KEY, isChecked) }
        }

        SoundToggle("Mute Alarms", muteAlarms, enabled = !isDnd) { isChecked ->
            coroutineScope.launch { muteSettingsManager.saveSetting(MuteSettingsManager.ALARMS_KEY, isChecked) }
        }

        SoundToggle("Mute Media", muteMedia, enabled = !isDnd) { isChecked ->
            coroutineScope.launch { muteSettingsManager.saveSetting(MuteSettingsManager.MEDIA_KEY, isChecked) }
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
