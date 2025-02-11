package com.example.mutemate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import com.example.mutemate.ui.theme.MuteMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "mute_schedule_db"
        ).build()

        val dao = database.muteScheduleDao()
        val viewModel = MuteViewModel(dao, this)

        setContent {
            MuteMateTheme {
                MuteScreen(viewModel, this)
            }
        }
    }
}