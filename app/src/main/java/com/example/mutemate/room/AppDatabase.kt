package com.example.mutemate.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mutemate.model.MuteSchedule

@Database(entities = [MuteSchedule::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun muteScheduleDao(): MuteScheduleDao
}
