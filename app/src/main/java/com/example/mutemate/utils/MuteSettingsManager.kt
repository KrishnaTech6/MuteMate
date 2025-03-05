package com.example.mutemate.utils

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("mute_settings")

class MuteSettingsManager(private val context: Context) {

    companion object {
         val DND_KEY = booleanPreferencesKey("dnd_mode")
         val RINGTONE_KEY = booleanPreferencesKey("mute_ringtone")
         val NOTIFICATIONS_KEY = booleanPreferencesKey("mute_notifications")
         val ALARMS_KEY = booleanPreferencesKey("mute_alarms")
         val MEDIA_KEY = booleanPreferencesKey("mute_media")
    }

    // Get saved values (Flow emits changes automatically)
    val isDnd: Flow<Boolean> = context.dataStore.data.map { it[DND_KEY] ?: false }
    val muteRingtone: Flow<Boolean> = context.dataStore.data.map { it[RINGTONE_KEY] ?: false }
    val muteNotifications: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATIONS_KEY] ?: false }
    val muteAlarms: Flow<Boolean> = context.dataStore.data.map { it[ALARMS_KEY] ?: false }
    val muteMedia: Flow<Boolean> = context.dataStore.data.map { it[MEDIA_KEY] ?: false }

    // Save a setting
    suspend fun saveSetting(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }

    // Update DND Mode (Enables all mute settings if true)
    suspend fun updateDndMode(enabled: Boolean) {
        saveSetting(DND_KEY, enabled)
    }
}