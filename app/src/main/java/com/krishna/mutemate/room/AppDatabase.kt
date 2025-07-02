package com.krishna.mutemate.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.krishna.mutemate.model.MuteSchedule

@Database(entities = [MuteSchedule::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun muteScheduleDao(): MuteScheduleDao

    companion object{
        @Volatile  // Ensures instant visibility of changes across threads
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) { // only one thread access at a time
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mute_schedule_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
