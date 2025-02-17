package com.example.mutemate.utils

import android.content.Context
import android.media.AudioManager

class MuteHelper(private val context: Context) {
//    fun mutePhone() {
//        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
//    }
//    fun vibrateModePhone() {
//        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
//    }
//
//    fun unmutePhone() {
//        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
//    }

    fun mutePhone() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)  // Mute Ringtone
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)  // Mute Notifications
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0)  // Mute Alarms
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)  // Mute Media
    }

    fun unmutePhone() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 5, 0)  // Restore Ringtone
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 5, 0)  // Restore Notifications
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 5, 0)  // Restore Alarms
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0)  // Restore Media
    }
}