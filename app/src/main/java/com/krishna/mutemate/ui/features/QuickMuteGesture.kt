package com.krishna.mutemate.ui.features

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.krishna.mutemate.utils.AccessibilityUtils
import com.krishna.mutemate.utils.MuteSettingsManager
import com.krishna.mutemate.utils.QUICK_MUTE_VIDEO_URL
import com.krishna.mutemate.utils.openWebLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun QuickMuteGesture(
    coroutineScope: CoroutineScope,
    context: Context,
    muteSettingsManager: MuteSettingsManager
) {
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    val isQuickMuteGestureEnabled by muteSettingsManager.isQuickMuteGestureEnabled.collectAsState(initial = false)
    val quickMuteDuration by muteSettingsManager.quickMuteDuration.collectAsState(initial = 30)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
            OutlinedButton(
                onClick = {
                    openWebLink(context, QUICK_MUTE_VIDEO_URL)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = "YouTube",
                    tint = Color(0xFFFF0000),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "How to enable Quick Mute",
                    color = Color(0xFFE53935), // YouTube-like Red
                    fontWeight = FontWeight.Bold
                )
            }
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
                            if (!isQuickMuteGestureEnabled) {
                                showAccessibilityDialog = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Kindly disable the accessibility service to disable the quick mute feature.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // go to settings to disable
                                context.startActivity(AccessibilityUtils.getAccessibilitySettingsIntent())
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mute Duration: $quickMuteDuration minute${if (quickMuteDuration > 1) "s" else ""}",
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
    // Show accessibility service dialog if needed
    if (showAccessibilityDialog) {
        var isConsentGiven by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAccessibilityDialog = false },
            title = { Text("Enable Accessibility Service") },
            text = {
                Column {
                    Text(
                        "Mute Mate uses the AccessibilityService API only to detect when you triple-press the volume down button. " +
                                "This is necessary for the Quick Mute feature to work from any screen, including the lock screen."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        buildAnnotatedString {
                            append("We ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)){
                                append("do NOT collect, store, or share")
                            }
                            append(" any personal or sensitive data. " +
                                    "All actions happen entirely on your device.")
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isConsentGiven,
                            onCheckedChange = { isConsentGiven = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("I understand and give consent.")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccessibilityDialog = false
                        context.startActivity(AccessibilityUtils.getAccessibilitySettingsIntent())
                    },
                    enabled = isConsentGiven
                ) {
                    Text("Enable Access")
                }
            },
            dismissButton = {
                Button(onClick = { showAccessibilityDialog = false }) {
                    Text("Later")
                }
            }
        )
    }
}