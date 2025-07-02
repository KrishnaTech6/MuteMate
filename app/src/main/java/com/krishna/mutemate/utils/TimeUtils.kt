package com.krishna.mutemate.utils

import android.util.Log
import java.util.Date

fun getTimeUntilStart(startTime: Date?): Int {
    if (startTime==null) return 0
    try {
        val diffInSec = ((startTime.time - System.currentTimeMillis()) / 1000).toInt()
        return diffInSec
    } catch (e: Exception) {
        Log.e("getTimeUntilStart", "Error parsing time: ${e.message}")
        return -1
    }
}


fun calculateDelay(time: Date?): Long {
    if(time == null) return 0
    val currentTime = System.currentTimeMillis()
    val targetTime = time
    return targetTime.time - currentTime
}

fun formatTimeRemaining(timeRemaining: Int, isEnd: Boolean= false ): String {
    val endOrStart= if(!isEnd) "Starts" else "Ends"
    return when {
        timeRemaining >= 3600 -> "$endOrStart in ${timeRemaining / 3600}h ${timeRemaining % 3600 / 60}m".trimEnd()
        timeRemaining >= 120 -> "$endOrStart in ${timeRemaining / 60}m"
        timeRemaining >= 60 -> "$endOrStart in 1m ${timeRemaining % 60}s"
        timeRemaining > 0 -> "$endOrStart in ${timeRemaining}s"
        else -> "Running"
    }
}
