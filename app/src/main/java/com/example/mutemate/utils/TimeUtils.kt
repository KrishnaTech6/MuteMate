package com.example.mutemate.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun convertToAmPm(time: String): String {
    val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    if (time.isNotEmpty()) {
        val date = inputFormat.parse(time)
        return outputFormat.format(date ?: time)
    }
    return ""
}

fun getTimeUntilStart(startTime: String): Int {
    if (startTime.isEmpty()) return 0

    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val now = Calendar.getInstance()
    val start = Calendar.getInstance()

    try {
        val parsedTime = dateFormat.parse(startTime) ?: return -1

        // Apply parsed hour and minute to today's date
        start.apply {
            set(Calendar.HOUR_OF_DAY, parsedTime.hours)
            set(Calendar.MINUTE, parsedTime.minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffInInSec = ((start.timeInMillis - now.timeInMillis) / 1000).toInt()
        return diffInInSec
    } catch (e: Exception) {
        Log.e("getTimeUntilStart", "Error parsing time: ${e.message}")
        return -1
    }
}

fun calculateDelay(time: String): Long {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val currentTime = Calendar.getInstance()
    val targetTime = Calendar.getInstance()

    try {
        val parsedTime = if (time.isEmpty()) return 0L else sdf.parse(time)
        targetTime.time = parsedTime

        // Ensure target time is on the same day or the next day if it's already passed
        targetTime.set(Calendar.YEAR, currentTime.get(Calendar.YEAR))
        targetTime.set(Calendar.MONTH, currentTime.get(Calendar.MONTH))
        targetTime.set(Calendar.DAY_OF_MONTH, currentTime.get(Calendar.DAY_OF_MONTH))

        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = targetTime.timeInMillis - currentTime.timeInMillis
        return delay
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return 0L
}