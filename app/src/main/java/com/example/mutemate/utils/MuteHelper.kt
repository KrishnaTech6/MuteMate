package com.example.mutemate.utils

import android.content.Context
import android.media.AudioManager

class MuteHelper(private val context: Context) {

    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun dndModeOn() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
    }

    fun normalMode() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
    }

    fun vibrateModePhone(muteSettings: MuteSettingsManager) {
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
    }

    fun mutePhone(
        muteRingtone: Boolean,
        muteNotifications: Boolean,
        muteAlarms: Boolean,
        muteMedia: Boolean
    ) {
        if(muteRingtone) audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)  // Mute Ringtone
        if(muteNotifications) audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)  // Mute Notifications
        if(muteAlarms) audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0)  // Mute Alarms
        if(muteMedia) audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)  // Mute Media
    }

    fun unmutePhone() {
        audioManager.setStreamVolume(
            AudioManager.STREAM_RING,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
            0
        )  // Restore Ringtone
        audioManager.setStreamVolume(
            AudioManager.STREAM_NOTIFICATION,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
            0
        )  // Restore Notifications
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            0
        )  // Restore Alarms
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )  // Restore Media
    }
}