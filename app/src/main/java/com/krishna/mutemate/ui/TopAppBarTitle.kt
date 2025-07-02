package com.krishna.mutemate.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.krishna.mutemate.R

@Composable
fun TopAppBarTitle() {
    Row(verticalAlignment = Alignment.CenterVertically){
        Image(painter = painterResource(R.drawable.mute_mate),null,
            Modifier
                .size(50.dp)
                .padding(8.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
        Spacer(Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.Center){
            Text(text = "MuteMate", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = "One-Touch mute scheduler",
                style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }

}