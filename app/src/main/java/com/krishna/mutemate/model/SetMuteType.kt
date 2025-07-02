package com.krishna.mutemate.model

data class SetMuteType(
    val muteMedia: Boolean =false ,
    val muteRingtone: Boolean =false ,
    val muteAlarm: Boolean =false ,
    val muteNotifications: Boolean =false ,
)