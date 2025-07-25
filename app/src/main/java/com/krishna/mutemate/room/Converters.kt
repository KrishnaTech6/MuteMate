package com.krishna.mutemate.room

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.krishna.mutemate.model.AllMuteOptions
import java.util.Date

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromAllMuteOptions(value: AllMuteOptions): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAllMuteOptions(value: String): AllMuteOptions {
        val type = object: TypeToken<AllMuteOptions>(){}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toLatLong(latLng: String): LatLng {
        val type = object: TypeToken<LatLng>(){}.type
        return gson.fromJson(latLng, type)
    }

    @TypeConverter
    fun fromLatLong(value: LatLng): String {
        return gson.toJson(value)
    }
}
