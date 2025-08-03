package com.krishna.mutemate.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.krishna.mutemate.R
import com.krishna.mutemate.utils.EMAIL
import com.krishna.mutemate.utils.sendEmailIntent

@Composable
fun AboutAppScreen(navController: NavHostController, modifier: Modifier) {

    val context = LocalContext.current
    val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Icon
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Icon",
            modifier = Modifier
                .size(96.dp)
                .padding(bottom = 8.dp)
        )

        // App Name
        Text(
            text = "MuteMate",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Version
        Text(
            text = "Version $versionName",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "MuteMate is your smart sound control companion. Whether you’re in a meeting, studying, attending a lecture, or simply need peace, " +
                    "MuteMate makes it effortless to silence your phone — instantly.\n" +
                    "\n" +
                    "Triple Volume-Down = Instant Mute \nJust press the volume-down button three times from anywhere on your phone." +
                    " No unlocking, no searching, no delay — your phone will instantly switch to Silent, Vibrate, or Do Not Disturb, " +
                    "depending on your pre-set choice.\n" +
                    "\n" +
                    "Set exact times for muting and unmuting automatically. " +
                    "Ideal for regular routines like classes, work hours, or bedtime.\n" +
                    "\n" +
                    "Clean, Simple UI\n" +
                    "Easy-to-use interface focused on functionality. No clutter, just fast control.\n" +
                    "\n" +
                    "Battery-Friendly & Lightweight\n" +
                    "MuteMate runs efficiently without draining your phone’s battery.\n" +
                    "\n" +
                    "MuteMate works locally on your device. No personal data is collected or shared.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Developer Info
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = "Developer")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Developed by Krishna Rana", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Contact Info
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Email, contentDescription = "Email")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                EMAIL,
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.clickable(onClick = { sendEmailIntent(context) })
            )
        }
        Spacer(modifier = Modifier.height(100.dp))
    }

}