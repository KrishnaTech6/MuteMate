package com.example.mutemate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mutemate.room.DatabaseProvider
import com.example.mutemate.ui.theme.MuteMateTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel: MuteViewModel by viewModels {
            MuteViewModelFactory(DatabaseProvider.getDatabase(applicationContext).muteScheduleDao(), application)
        }
        setContent {
            MuteMateTheme {
                val context = applicationContext
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()
                var showBottomSheet by remember { mutableStateOf(false) }
                val bottomSheetState = rememberModalBottomSheetState()
                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Row{
                                    Image(painter = painterResource(id = R.mipmap.ic_launcher_foreground),null, Modifier.size(50.dp))
                                    Column(verticalArrangement = Arrangement.Center){
                                        Text(text = "MuteMate", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                        Text(text = "One-Touch mute scheduler",
                                            style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                                    }
                                }
                            },
                            actions = {
                                Icon(Icons.Default.MoreVert, null, Modifier.padding(4.dp)
                                .clickable{ showBottomSheet = true })
                            }
                        )
                    }) { padding ->
                    MuteScreen(snackbarHostState,coroutineScope, viewModel, modifier = Modifier.padding(padding))
                }

                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = bottomSheetState
                    ) {
                        SilentModeSettingsScreen(context, Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}