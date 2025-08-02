package com.krishna.mutemate.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.krishna.mutemate.model.AllMuteOptions
import com.krishna.mutemate.model.SetMuteType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("mute_settings")

class MuteSettingsManager(private val context: Context) {

    companion object {
         val IS_DND_KEY = booleanPreferencesKey("isDnd")
         val IS_MUTE_KEY = booleanPreferencesKey("isMute")
         val IS_VIBRATE_KEY = booleanPreferencesKey("isVibrate")
         val MUTE_RINGTONE_KEY = booleanPreferencesKey("mute_ringtone")
         val MUTE_NOTIFICATIONS_KEY = booleanPreferencesKey("mute_notifications")
         val MUTE_ALARM_KEY = booleanPreferencesKey("mute_alarms")
         val MUTE_MEDIA_KEY = booleanPreferencesKey("mute_media")
         val QUICK_MUTE_DURATION_KEY = intPreferencesKey("quick_mute_duration")
         val QUICK_MUTE_ENABLED = booleanPreferencesKey("quick_mute_enabled")

        val THEME_MODE = stringPreferencesKey("theme_mode") // "light", "dark", "system"
    }

    // Get saved values (Flow emits changes automatically)
    val isQuickMuteGestureEnabled: Flow<Boolean> = context.dataStore.data.map { it[QUICK_MUTE_ENABLED] ?: false }
    val quickMuteDuration: Flow<Int> = context.dataStore.data.map { it[QUICK_MUTE_DURATION_KEY] ?: 30 } // Default 30 minutes

    val allMuteOptions: Flow<AllMuteOptions> =context.dataStore.data.map { prefs ->
        AllMuteOptions(
            isDnd = prefs[IS_DND_KEY] ?: false,
            isVibrate = prefs[IS_VIBRATE_KEY] ?: false,
            isMute = prefs[IS_MUTE_KEY] ?: true,
            muteType = SetMuteType(
                muteMedia = prefs[MUTE_MEDIA_KEY] ?: false,
                muteRingtone = prefs[MUTE_RINGTONE_KEY] ?: false,
                muteAlarm = prefs[MUTE_ALARM_KEY] ?: false,
                muteNotifications = prefs[MUTE_NOTIFICATIONS_KEY] ?: false
            )
        )
    }
    // Save a setting
    suspend fun saveSetting(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }

    suspend fun saveAllSettings(options: AllMuteOptions){
        context.dataStore.edit { prefs ->
            prefs[IS_DND_KEY] = options.isDnd
            prefs[IS_VIBRATE_KEY] = options.isVibrate
            prefs[IS_MUTE_KEY] = options.isMute
            prefs[MUTE_MEDIA_KEY] = options.muteType.muteMedia
            prefs[MUTE_RINGTONE_KEY] = options.muteType.muteRingtone
            prefs[MUTE_ALARM_KEY] = options.muteType.muteAlarm
            prefs[MUTE_NOTIFICATIONS_KEY] = options.muteType.muteNotifications
        }
    }

    // Save integer setting
    suspend fun saveIntSetting(key: Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { it[key] = value }
    }

    // Update quick mute duration
    suspend fun updateQuickMuteDuration(minutes: Int) {
        saveIntSetting(QUICK_MUTE_DURATION_KEY, minutes)
        Log.d("TAG", "updateQuickMuteDuration: $minutes")
    }

    suspend fun saveThemeMode(context: Context, mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    fun getThemeSettings(context: Context) = context.dataStore.data.map { prefs ->
       prefs[THEME_MODE] ?: "system"
    }
}