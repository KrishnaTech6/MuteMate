package com.krishna.mutemate.model

data class AllMuteOptions(
    val isDnd: Boolean= false,
    val isVibrate: Boolean =false,
    val isMute: Boolean = false,
    val muteType: SetMuteType = SetMuteType()
){
    fun isValid(): Boolean {
        return isDnd || isVibrate || isMute ||
                muteType.muteRingtone || muteType.muteMedia ||
                muteType.muteNotifications || muteType.muteAlarm
    }
}