package com.krishna.mutemate.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.ui.graphics.vector.ImageVector


enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("songs", "Songs", Icons.Default.Home, "Songs"),
    LIST("list", "List", Icons.Default.FormatListBulleted, "Album"),
    MAP("map", "Map", Icons.Default.PinDrop, "Map"),
}