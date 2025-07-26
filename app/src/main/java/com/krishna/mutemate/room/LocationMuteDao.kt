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
    suspend fun insertLocationMute(locationMute: LocationMute) : Long

    @Query("SELECT * FROM location_mute")
    fun getAllLocationMutes(): Flow<List<LocationMute>>

    @Delete
    suspend fun deleteLocationMute(locationMute: LocationMute)

    @Query("SELECT * FROM location_mute WHERE id = :id")
    suspend fun getLocationById(id: Long): LocationMute?
}