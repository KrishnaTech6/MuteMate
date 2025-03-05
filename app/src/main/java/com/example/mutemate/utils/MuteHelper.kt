package com.example.mutemate.utils

import android.content.Context
import android.media.AudioManager

class MuteHelper(private val context: Context) {

    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//    val previousVolume = listOf(
//        audioManager.getStreamVolume(AudioManager.STREAM_RING),
//        audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION),
//        audioManager.getStreamVolume(AudioManager.STREAM_ALARM),
//        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
//    )

    fun dndModeOn() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
    }

    fun normalMode() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
    }

    fun vibrateModePhone() {
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
    }

    fun mutePhone() {
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)  // Mute Ringtone
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)  // Mute Notifications
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0)  // Mute Alarms
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)  // Mute Media
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