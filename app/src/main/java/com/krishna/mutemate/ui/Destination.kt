package com.krishna.mutemate.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector


enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("home", "Home", Icons.Default.Home, "home"),
    LIST("list", "List", Icons.Default.FormatListBulleted, "Album"),
    MAP("map", "Map", Icons.Default.PinDrop, "Map"),
    SETTINGS("settings", "Settings", Icons.Default.Settings, "Settings"),
}