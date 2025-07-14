package com.krishna.mutemate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview(showBackground = true)
fun ButtonMute(
    color: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector = Icons.Default.AlarmOff,
    isSelected: Boolean = true,
    title: String = "Mute",
    modifier: Modifier = Modifier,
    onItemClick: (Boolean) -> Unit = {},
) {
    val colorNew = if(isSelected) color.copy(0.1f) else Color.White
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colorNew)
            .border(1.dp, color, RoundedCornerShape(12.dp))
            .clickable { onItemClick(isSelected) }
            .padding(16.dp)
            ,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.size(40.dp)
        ){
            Icon(
                imageVector = icon,
                contentDescription = when (title) {
                    "DND" -> "Enable Do Not Disturb mode. All sounds off except alarms unless customized."
                    "Mute" -> "Mute all system sounds except alarms and media."
                    "Vibrate" -> "Enable vibrate-only mode. No sound, phone will vibrate for calls."
                    else -> title
                },
                tint = color
            )
            Text(
                text =title,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
            Text(
                text = when (title) {
                    "DND" -> "Mute all. Only alarms or allowed calls."
                    "Mute" -> "Silence calls and notifications."
                    "Vibrate" -> "No sound, only vibrate."
                    else -> ""
                },
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f)
            )
        }

    }
}