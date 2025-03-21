package com.example.mutemate.utils

import android.content.Context
import android.os.BatteryManager

fun isBatteryLow(context: Context): Boolean {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

    return batteryLevel <= 15 // Consider battery low if 20% or less
}