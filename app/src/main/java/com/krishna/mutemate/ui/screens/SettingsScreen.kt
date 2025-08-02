package com.krishna.mutemate.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.krishna.mutemate.ui.Destination
import com.krishna.mutemate.utils.HOW_TO_USE
import com.krishna.mutemate.utils.MuteSettingsManager
import com.krishna.mutemate.utils.PRIVACY_POLICY
import com.krishna.mutemate.utils.openWebLink
import com.krishna.mutemate.utils.rateApp
import com.krishna.mutemate.utils.sendEmailIntent
import com.krishna.mutemate.utils.shareApp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, context: Context = LocalContext.current, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val themePreferences = MuteSettingsManager(context)
    val currentMode by themePreferences.getThemeSettings(context).collectAsState(initial = "system")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        item {
            SettingsSection(title = "Appearance")
            ThemeModeSelector(
                currentMode = currentMode,
                onModeChange = { mode ->
                    scope.launch {
                        themePreferences.saveThemeMode(context, mode)
                    }
                }
            )
        }
        item {
            // General Section
            SettingsSection(title = "General")
            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                onClick = {
                    openWebLink(context, PRIVACY_POLICY)
                }
            )
            SettingsItem(
                icon = Icons.Default.Help,
                title = "How to Use",
                onClick = {
                    openWebLink(context, HOW_TO_USE)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Support Section
            SettingsSection(title = "Support")
            SettingsItem(
                icon = Icons.Default.Feedback,
                title = "Send Feedback Email",
                onClick = { sendEmailIntent(context) }
            )
            SettingsItem(
                icon = Icons.Default.Share,
                title = "Share App",
                onClick = { shareApp(context) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            // About Section
            SettingsSection(title = "About")
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About App",
                onClick = { navController.navigate(Destination.AboutApp.route) }
            )
            SettingsItem(
                icon = Icons.Default.StarRate,
                title = "Rate Us",
                onClick = { rateApp(context)}
            )
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Go",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
fun ThemeModeSelector(
    currentMode: String,
    onModeChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)){
        listOf("system", "light", "dark").forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onModeChange(mode) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentMode == mode,
                    onClick = { onModeChange(mode) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(mode.replaceFirstChar { it.uppercase() })
            }
        }
    }
}
