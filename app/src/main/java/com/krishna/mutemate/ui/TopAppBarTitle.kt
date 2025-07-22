package com.krishna.mutemate.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.navigation.NavHostController
import com.krishna.mutemate.R

@Composable
fun TopAppBarTitle(navController: NavHostController, selectedDestination: Int) {
    when(selectedDestination){
        Destination.HOME.ordinal -> {
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
        Destination.LIST.ordinal -> {
            TopTitle(navController, "Schedule List")
        }

        Destination.MAP.ordinal ->{
            TopTitle(navController,"Select Location to Mute")
        }
    }
}

@Composable
private fun TopTitle(navController: NavHostController, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack, null,
            Modifier
                .padding(8.dp)
                .clickable {
                    navController.popBackStack()
                },
        )
        Spacer(Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}