package com.example.mutemate

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.mutemate.room.DatabaseProvider
import com.example.mutemate.ui.MuteScreen
import com.example.mutemate.ui.SilentModeSettingsScreen
import com.example.mutemate.ui.TopAppBarTitle
import com.example.mutemate.ui.theme.MuteMateTheme
import com.example.mutemate.utils.AccessibilityUtils
import com.example.mutemate.utils.NotificationHelper
import com.example.mutemate.viewmodel.MuteViewModel
import com.example.mutemate.viewmodel.MuteViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
        // Request notification permission
        requestNotificationPermission()
        // Check accessibility service
        checkAccessibilityService()

        val viewModel: MuteViewModel by viewModels {
            MuteViewModelFactory(
                DatabaseProvider.getDatabase(applicationContext).muteScheduleDao(),
                application
            )
        }
        setContent {
            MuteMateTheme {
                val context = applicationContext
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()
                var showBottomSheet by remember { mutableStateOf(false) }
                val bottomSheetState = rememberModalBottomSheetState()
                var showAccessibilityDialog by remember { mutableStateOf(!AccessibilityUtils.isAccessibilityServiceEnabled(context)) }

                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = { TopAppBarTitle() },
                            actions = {
                                Icon(
                                    Icons.Default.MoreVert,
                                    null,
                                    Modifier
                                        .padding(4.dp)
                                        .clickable { showBottomSheet = true })
                            }
                        )
                    }) { padding ->
                    MuteScreen(
                        snackbarHostState,
                        coroutineScope,
                        viewModel,
                        modifier = Modifier.padding(padding)
                    )
                }

                if (showBottomSheet) {
                    SilentModeSettingsScreen(
                        onDismissRequest = { showBottomSheet = false },
                        bottomSheetState = bottomSheetState,
                        context = context,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Show accessibility service dialog if needed
                if (showAccessibilityDialog) {
                    AlertDialog(
                        onDismissRequest = { showAccessibilityDialog = false },
                        title = { Text("Enable Accessibility Service") },
                        text = { Text("To use the volume button triple-press feature, you need to enable the MuteMate accessibility service.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showAccessibilityDialog = false
                                    startActivity(AccessibilityUtils.getAccessibilitySettingsIntent())
                                }
                            ) {
                                Text("Enable")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showAccessibilityDialog = false }
                            ) {
                                Text("Later")
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check accessibility service when returning to the app
        checkAccessibilityService()
    }

    private fun checkAccessibilityService() {
        // The actual check is done in the Composable using AccessibilityUtils
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
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
}