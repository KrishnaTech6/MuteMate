package com.krishna.mutemate.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String, val label: String, val icon: ImageVector? = null, val contentDescription: String?= null){
    object Home : Destination("home", "Home", Icons.Default.Home, "home")
    object List : Destination("list", "List", Icons.AutoMirrored.Filled.FormatListBulleted, "Album")
    object Map : Destination("map", "Map", Icons.Default.PinDrop, "Map")
    object Settings : Destination("settings", "Settings",Icons.Default.Settings, "Settings")

    object AboutApp: Destination("aboutApp", "About App")

    companion object{
        val entries = listOf(Home, List, Map, Settings)
    }
}