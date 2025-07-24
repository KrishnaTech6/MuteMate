package com.krishna.mutemate.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefUtils {
    fun saveList(context: Context, list: List<Int>, key: String = KEY_LIST) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            val json = Gson().toJson(list)
            putString(key, json)
        }
    }
    fun getList(context: Context, key: String = KEY_LIST): List<Int>? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(key, null)
        val type = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    fun getString(context: Context, key: String): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(key, null)
    }
    fun getBoolean(context: Context, key: String): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(key, false)
    }

    fun saveString(
        context: Context,
        text: String,
        key: String
    ){
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(key, text)
        editor.apply()
    }

    fun saveBoolean(
        context: Context,
        isChecked: Boolean,
        key: String
    ){
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(key, isChecked)
        }
    }
}