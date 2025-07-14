package com.krishna.mutemate

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.lifecycle.lifecycleScope
import com.krishna.mutemate.ui.MuteScreen
import com.krishna.mutemate.ui.SilentModeSettingsScreen
import com.krishna.mutemate.ui.TopAppBarTitle
import com.krishna.mutemate.ui.theme.MuteMateTheme
import com.krishna.mutemate.utils.AccessibilityUtils
import com.krishna.mutemate.utils.MuteSettingsManager
import com.krishna.mutemate.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
        // Request notification permission
        requestNotificationPermission()
        setContent {
            MuteMateTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()
                var showBottomSheet by remember { mutableStateOf(false) }
                val bottomSheetState = rememberModalBottomSheetState()

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
                        modifier = Modifier.padding(padding)
                    )
                }

                if (showBottomSheet) {
                    SilentModeSettingsScreen(
                        onDismissRequest = { showBottomSheet = false },
                        bottomSheetState = bottomSheetState,
                        context = this,
                        modifier = Modifier.safeContentPadding()
                    )
                }
            }
        }
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

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (AccessibilityUtils.isAccessibilityServiceEnabled(this@MainActivity)) {
                MuteSettingsManager(this@MainActivity).saveSetting(MuteSettingsManager.QUICK_MUTE_ENABLED, true)
            }
        }
    }
}