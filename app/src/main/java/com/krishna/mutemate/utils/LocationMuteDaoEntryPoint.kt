package com.krishna.mutemate.utils

import com.krishna.mutemate.room.LocationMuteDao

interface LocationMuteDaoEntryPoint {
    fun locationMuteDao(): LocationMuteDao
}