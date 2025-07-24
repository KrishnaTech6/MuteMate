package com.krishna.mutemate.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.util.Date

@Entity(tableName = "mute_schedule")
data class MuteSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Date?,
    val endTime: Date?,
    val muteOptions: AllMuteOptions,
    val radius: Int = 0,
    val latLng: LatLng = LatLng(0.0, 0.0),
    val address: String = "",
)