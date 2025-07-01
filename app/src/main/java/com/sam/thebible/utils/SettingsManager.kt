package com.sam.thebible.utils

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bible_settings", Context.MODE_PRIVATE)
    
    var showEnglish: Boolean
        get() = prefs.getBoolean("show_english", true)
        set(value) = prefs.edit().putBoolean("show_english", value).apply()
    
    var fontSize: Int
        get() = prefs.getInt("font_size", 5)
        set(value) = prefs.edit().putInt("font_size", value).apply()
    
    var fontColorIndex: Int
        get() = prefs.getInt("font_color", 0)
        set(value) = prefs.edit().putInt("font_color", value).apply()
    
    var backgroundColorIndex: Int
        get() = prefs.getInt("background_color", 1)
        set(value) = prefs.edit().putInt("background_color", value).apply()
    
    var isDarkMode: Boolean
        get() = prefs.getBoolean("dark_mode", true)
        set(value) = prefs.edit().putBoolean("dark_mode", value).apply()
    
    var lastBookCode: String
        get() = prefs.getString("last_book", "") ?: ""
        set(value) = prefs.edit().putString("last_book", value).apply()
    
    var lastChapter: Int
        get() = prefs.getInt("last_chapter", 1)
        set(value) = prefs.edit().putInt("last_chapter", value).apply()
}