package com.krishna.mutemate.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krishna.mutemate.model.LocationMute
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationMuteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationMute(locationMute: LocationMute)

    @Query("SELECT * FROM locate_mute")
    fun getAllLocationMutes(): Flow<List<LocationMute>>

    @Delete
    suspend fun deleteLocationMute(locationMute: LocationMute)
}