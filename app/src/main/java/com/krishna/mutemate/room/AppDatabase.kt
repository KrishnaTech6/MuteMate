package com.krishna.mutemate.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.krishna.mutemate.model.MuteSchedule

@Database(entities = [MuteSchedule::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun muteScheduleDao(): MuteScheduleDao
}
