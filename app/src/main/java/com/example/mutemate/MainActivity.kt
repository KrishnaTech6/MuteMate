package com.example.mutemate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.mutemate.room.DatabaseProvider
import com.example.mutemate.ui.theme.MuteMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel: MuteViewModel by viewModels {
            MuteViewModelFactory(DatabaseProvider.getDatabase(applicationContext).muteScheduleDao(), application)
        }
        setContent {
            MuteMateTheme {
                Scaffold { padding ->
                    MuteScreen(viewModel, modifier = Modifier.padding(padding))
                }
            }
        }
    }
}