package com.krishna.mutemate.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.krishna.mutemate.ui.MainActivity.Dest.MAIN
import com.krishna.mutemate.ui.MainActivity.Dest.ONBOARDING
import com.krishna.mutemate.ui.screens.OnBoardingScreen
import com.krishna.mutemate.ui.theme.MuteMateTheme
import com.krishna.mutemate.utils.AccessibilityUtils
import com.krishna.mutemate.utils.IS_ONBOARDING_DONE
import com.krishna.mutemate.utils.MuteSettingsManager
import com.krishna.mutemate.utils.NotificationHelper
import com.krishna.mutemate.utils.SharedPrefUtils.getBoolean
import com.krishna.mutemate.utils.SharedPrefUtils.saveBoolean
import com.krishna.mutemate.utils.UpdateHelper
import com.krishna.mutemate.viewmodel.MuteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var updateHelper: UpdateHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        updateHelper = UpdateHelper(this)
        // Check for update (Flexible mode)
        updateHelper.checkForAppUpdate(immediate = false)

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
        // Request notification permission
        requestNotificationPermission()
        setContent {
            val themeMode by MuteSettingsManager(this).getThemeSettings(this).collectAsState(initial = "system")
            val view = LocalView.current

            MuteMateTheme(window ,view, themeMode) {
                val navController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()
                val startDestination = if (getBoolean(this, IS_ONBOARDING_DONE)) MAIN else ONBOARDING

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(ONBOARDING) {
                        OnBoardingScreen(
                            onGetStarted = {
                                // Save onboarding flag and navigate to Main
                                coroutineScope.launch {
                                    saveBoolean(this@MainActivity, true, IS_ONBOARDING_DONE)
                                    navController.navigate(MAIN) {
                                        popUpTo(ONBOARDING) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                    composable(MAIN) {
                        MainAppScreen()
                    }
                }
            }
        }
    }
    object Dest{
        const val ONBOARDING = "onboarding"
        const val MAIN = "main"
    }
    @Composable
    fun MainAppScreen() {
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination?.route
        val selectedDestination =
            Destination.entries.indexOfFirst { it.route == currentDestination }.coerceAtLeast(0)
        val viewModel = hiltViewModel<MuteViewModel>()

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { TopAppBarTitle(navController, selectedDestination) },
                )
            },
            bottomBar = {
                BottomNavigationBar(navController, selectedDestination)
            }
        ) { padding ->
            NavHostApp(
                navController,
                snackbarHostState,
                coroutineScope,
                Modifier.padding(padding),
                viewModel
            )
        }
    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, now we can show notifications
                Toast.makeText(this@MainActivity, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied, handle accordingly (optional)
                Toast.makeText(this@MainActivity, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onResume() {
        super.onResume()
        updateHelper.onResumeCheck()
        lifecycleScope.launch {
            if (AccessibilityUtils.isAccessibilityServiceEnabled(this@MainActivity)) {
                MuteSettingsManager(this@MainActivity).saveSetting(MuteSettingsManager.Companion.QUICK_MUTE_ENABLED, true)
            }else{
                MuteSettingsManager(this@MainActivity).saveSetting(MuteSettingsManager.Companion.QUICK_MUTE_ENABLED, false)
            }
        }
    }
}