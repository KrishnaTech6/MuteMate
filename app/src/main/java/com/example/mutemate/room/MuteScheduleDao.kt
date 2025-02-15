package com.example.mutemate.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mutemate.model.MuteSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface MuteScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: MuteSchedule):Long

    @Query("SELECT * FROM mute_schedule ORDER BY id DESC")
    fun getSchedules(): Flow<List<MuteSchedule>>

    @Delete
    suspend fun delete(schedule: MuteSchedule)

    @Query("DELETE FROM mute_schedule WHERE id = :scheduleId")
    suspend fun deleteId(scheduleId: Int)

}