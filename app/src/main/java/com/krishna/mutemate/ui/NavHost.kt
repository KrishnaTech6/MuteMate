package com.krishna.mutemate.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.krishna.mutemate.ui.screens.AboutAppScreen
import com.krishna.mutemate.ui.screens.MuteListScreen
import com.krishna.mutemate.ui.screens.MuteScreen
import com.krishna.mutemate.ui.screens.SettingsScreen
import com.krishna.mutemate.ui.screens.map.MapScreen
import com.krishna.mutemate.viewmodel.MapViewModel
import com.krishna.mutemate.viewmodel.MuteViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun NavHostApp(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier,
    muteViewModel: MuteViewModel = hiltViewModel(),
    mapViewModel: MapViewModel = hiltViewModel(),
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.Home -> MuteScreen(
                        navController = navController,
                        viewModel = muteViewModel,
                        snackbarHostState = snackbarHostState,
                        coroutineScope = coroutineScope,
                        modifier = modifier,
                    )
                    Destination.List -> MuteListScreen(
                        muteViewModel = muteViewModel,
                        mapViewModel = mapViewModel,
                        snackbarHostState = snackbarHostState,
                        coroutineScope = coroutineScope,
                        modifier = modifier
                    )
                    Destination.Map -> MapScreen(modifier = modifier)
                    Destination.Settings -> SettingsScreen(navController= navController, modifier = modifier)
                    else ->{}
                }
            }
        }
        composable(Destination.AboutApp.route) {
            AboutAppScreen(navController = navController, modifier = modifier)
        }
    }
}

