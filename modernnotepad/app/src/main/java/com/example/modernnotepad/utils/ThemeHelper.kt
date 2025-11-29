package com.example.modernnotepad.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    private const val THEME_PREF_KEY = "theme_preference"
    
    fun applyThemeFromPreferences(context: Context) {
        val sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        val themePreference = sharedPreferences.getString(THEME_PREF_KEY, "system")
        
        when (themePreference) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    fun saveThemePreference(sharedPreferences: SharedPreferences, themeValue: String) {
        sharedPreferences.edit().putString(THEME_PREF_KEY, themeValue).apply()
    }
}