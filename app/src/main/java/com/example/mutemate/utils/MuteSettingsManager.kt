package com.example.mutemate.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("mute_settings")

class MuteSettingsManager(private val context: Context) {

    companion object {
         val DND_KEY = booleanPreferencesKey("dnd_mode")
         val VIBRATION_KEY = booleanPreferencesKey("vibration_mode")
         val RINGTONE_KEY = booleanPreferencesKey("mute_ringtone")
         val NOTIFICATIONS_KEY = booleanPreferencesKey("mute_notifications")
         val ALARMS_KEY = booleanPreferencesKey("mute_alarms")
         val MEDIA_KEY = booleanPreferencesKey("mute_media")
         val QUICK_MUTE_DURATION_KEY = intPreferencesKey("quick_mute_duration")
         val QUICK_MUTE_ENABLED = booleanPreferencesKey("quick_mute_enabled")
    }

    // Get saved values (Flow emits changes automatically)
    val isDnd: Flow<Boolean> = context.dataStore.data.map { it[DND_KEY] ?: true } //Initially set to true
    val isVibrate: Flow<Boolean> = context.dataStore.data.map { it[VIBRATION_KEY] ?: false }
    val muteRingtone: Flow<Boolean> = context.dataStore.data.map { it[RINGTONE_KEY] ?: false }
    val muteNotifications: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATIONS_KEY] ?: false }
    val muteAlarms: Flow<Boolean> = context.dataStore.data.map { it[ALARMS_KEY] ?: false }
    val muteMedia: Flow<Boolean> = context.dataStore.data.map { it[MEDIA_KEY] ?: false }
    val isQuickMuteGestureEnabled: Flow<Boolean> = context.dataStore.data.map { it[QUICK_MUTE_ENABLED] ?: false }
    val quickMuteDuration: Flow<Int> = context.dataStore.data.map { it[QUICK_MUTE_DURATION_KEY] ?: 30 } // Default 30 minutes

    // Save a setting
    suspend fun saveSetting(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }

    // Save integer setting
    suspend fun saveIntSetting(key: Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { it[key] = value }
    }

    // Update DND Mode (Enables all mute settings if true)
    suspend fun updateDndMode(enabled: Boolean) {
        saveSetting(DND_KEY, enabled)
        Log.d("TAG", "updateDndMode: $enabled")
    }
    // Update DND Mode (Enables all mute settings if true)
    suspend fun updateVibrationMode(enabled: Boolean) {
        saveSetting(VIBRATION_KEY, enabled)
        Log.d("TAG", "updateVibrationMode: $enabled")
    }
    
    // Update quick mute duration
    suspend fun updateQuickMuteDuration(minutes: Int) {
        saveIntSetting(QUICK_MUTE_DURATION_KEY, minutes)
        Log.d("TAG", "updateQuickMuteDuration: $minutes")
    }
}