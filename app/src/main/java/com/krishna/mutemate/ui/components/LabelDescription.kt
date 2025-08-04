package com.krishna.mutemate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LabelDescription(modifier: Modifier = Modifier, title: String, description: String = "") {
    Column(modifier){
        Text(title, style = MaterialTheme.typography.labelLarge)
        if(description.isNotEmpty()){
            Box(Modifier.background(MaterialTheme.colorScheme.surfaceContainer.copy(0.5f)).padding(2.dp)) {
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}