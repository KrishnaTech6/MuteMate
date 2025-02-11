package com.example.mutemate

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MuteScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: MuteSchedule)

    @Query("SELECT * FROM mute_schedule")
    suspend fun getSchedules(): List<MuteSchedule>
}