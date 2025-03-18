package com.example.mutemate.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefUtils {
    private const val PREF_NAME = "MyPrefs"
    private const val KEY_LIST = "my_list"

    fun saveList(context: Context, list: List<Int>, key: String = KEY_LIST) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val gson = Gson()
        val json = gson.toJson(list) // Convert list to JSON
        editor.putString(key, json)
        editor.apply()
    }
    fun getList(context: Context, key: String = KEY_LIST): List<Int>? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(key, null)

        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }


    fun getString(context: Context, key: String): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(key, null)
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
}