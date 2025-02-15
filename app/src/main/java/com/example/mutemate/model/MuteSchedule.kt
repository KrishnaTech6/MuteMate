package com.example.mutemate.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mute_schedule")
data class MuteSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: String,
    val endTime: String
)