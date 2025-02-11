package com.example.mutemate

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MuteSchedule::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun muteScheduleDao(): MuteScheduleDao
}
