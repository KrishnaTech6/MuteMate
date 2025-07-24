package com.krishna.mutemate.model

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "locate_mute")
data class LocationMute(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0 ,
    val title: String? = null,
    @DrawableRes
    val markerType: Int,
    val muteOptions: AllMuteOptions,
    val radius: Int? = null,
    val latLng: LatLng? = null,
)
