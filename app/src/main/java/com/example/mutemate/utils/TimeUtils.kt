package com.example.mutemate.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun getTimeUntilStart(startTime: String): Int {
    if (startTime.isEmpty()) return 0

    val dateFormat = SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault())
    val now = Calendar.getInstance()
    val start = Calendar.getInstance()

    try {
        val parsedTime = dateFormat.parse(startTime) ?: return -1
        start.time = parsedTime // Correct way to apply parsed time

        val diffInSec = ((start.timeInMillis - now.timeInMillis) / 1000).toInt()
        return diffInSec
    } catch (e: Exception) {
        Log.e("getTimeUntilStart", "Error parsing time: ${e.message}")
        return -1
    }
}


fun calculateDelay(time: String): Long {
    val sdf = SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault())
    val currentTime = Calendar.getInstance()
    val targetTime = Calendar.getInstance()

    return try {
        if (time.isEmpty()) return 0L
        val parsedDate = sdf.parse(time) ?: return 0L

        // Set parsed time to targetTime
        targetTime.time = parsedDate

        // Ensure milliseconds are zero to avoid minor mismatches
        targetTime.set(Calendar.SECOND, 0)
        targetTime.set(Calendar.MILLISECOND, 0)

        // If the target time is in the past, set it to the next day
        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Calculate delay
        targetTime.timeInMillis - currentTime.timeInMillis
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }
}
