package com.krishna.mutemate.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.krishna.mutemate.ui.screens.MapScreen
import com.krishna.mutemate.ui.screens.MuteScreen
import com.krishna.mutemate.ui.screens.ScheduleListScreen
import com.krishna.mutemate.ui.screens.SettingsScreen
import com.krishna.mutemate.viewmodel.MuteViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun NavHostApp(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier,
    viewModel: MuteViewModel = hiltViewModel(),
) {
    NavHost(
        navController = navController,
        startDestination = Destination.HOME.route
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.HOME -> MuteScreen(
                        navController = navController,
                        viewModel = viewModel,
                        snackbarHostState,
                        coroutineScope,
                        modifier = modifier,
                    )
                    Destination.LIST -> ScheduleListScreen(
                        viewModel = viewModel,
                        snackbarHostState,
                        coroutineScope,
                        modifier = modifier
                    )

                    Destination.MAP -> MapScreen(modifier = modifier)
                    Destination.SETTINGS -> SettingsScreen(navController, modifier = modifier)
                }
            }
        }
    }
}