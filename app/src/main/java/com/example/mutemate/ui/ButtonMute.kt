package com.example.mutemate.ui

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
                contentDescription = "Mute",
                tint = color
            )
            Text(
                text =title,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }

    }
}