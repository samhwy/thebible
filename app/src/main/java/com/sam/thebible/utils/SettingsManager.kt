package com.sam.thebible.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Properties

class SettingsManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bible_settings", Context.MODE_PRIVATE)
    private val backupFile = File(context.filesDir, "bible_position_backup.properties")
    private val TAG = "SettingsManager"

    init {
        // Always try to restore from file on initialization
            restorePositionFromFile()

        // Log current values after initialization
        Log.d(TAG, "SettingsManager initialized - Book: $lastBookCode, Chapter: $lastChapter")
        }
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
        set(value) {
            prefs.edit().putString("last_book", value).commit() // Using commit() for immediate write
            savePositionToFile()
            Log.d(TAG, "lastBookCode set to: $value")
        }

    var lastChapter: Int
        get() = prefs.getInt("last_chapter", 1)
        set(value) {
            prefs.edit().putInt("last_chapter", value).commit() // Using commit() for immediate write
            savePositionToFile()
            Log.d(TAG, "lastChapter set to: $value")
        }

    /**
     * Save the current position (book and chapter) to a physical file
     */
    private fun savePositionToFile() {
        val properties = Properties()
        val currentBook = lastBookCode
        val currentChapter = lastChapter
        properties.setProperty("last_book", currentBook)
        properties.setProperty("last_chapter", currentChapter.toString())

        try {
            // Ensure the parent directory exists
            backupFile.parentFile?.mkdirs()

            FileOutputStream(backupFile).use { output ->
                properties.store(output, "Bible Position Backup")
                Log.d(TAG, "Position saved to file: Book=$currentBook, Chapter=$currentChapter")
            }

            // Verify file was created and has content
            if (backupFile.exists() && backupFile.length() > 0) {
                Log.d(TAG, "Backup file successfully created at: ${backupFile.absolutePath}")
            } else {
                Log.e(TAG, "Backup file creation failed or file is empty")
        }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save position to file", e)
        }
    }

    /**
     * Restore the position (book and chapter) from the physical file
     */
    private fun restorePositionFromFile() {
        if (!backupFile.exists()) {
            Log.d(TAG, "Backup file does not exist yet at: ${backupFile.absolutePath}")
            return
        }

        Log.d(TAG, "Found backup file at: ${backupFile.absolutePath}, size: ${backupFile.length()} bytes")

        try {
            val properties = Properties()
            FileInputStream(backupFile).use { input ->
                properties.load(input)

                lastBookCode = properties.getProperty("last_book")
                lastChapter = properties.getProperty("last_chapter")?.toIntOrNull() ?: 1

                Log.d(TAG, "Read from backup file: Book=$lastBookCode, Chapter=$lastChapter")

                 /* if (!book.isNullOrEmpty()) {
                    // Update SharedPreferences with values from file
                    val editor = prefs.edit()
                    editor.putString("last_book", book)
                    editor.putInt("last_chapter", chapter)
                     val success = editor.commit() // Using commit() for immediate write

                    if (success) {
                        Log.d(TAG, "Position successfully restored to SharedPreferences")
                    } else {
                        Log.e(TAG, "Failed to commit position to SharedPreferences")
                    }
                } */
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to restore position from file", e)
        }
    }

    /**
     * Force save the current position to both SharedPreferences and file
     * Call this method when the app is being closed
     */
    fun saveCurrentPosition(bookCode: String, chapter: Int) {
        if (bookCode.isEmpty()) {
            Log.w(TAG, "Attempted to save empty book code, ignoring")
            return
    }

        Log.d(TAG, "Saving current position: Book=$bookCode, Chapter=$chapter")

        // Update SharedPreferences directly with commit() for immediate persistence
        val editor = prefs.edit()
        editor.putString("last_book", bookCode)
        editor.putInt("last_chapter", chapter)
        val success = editor.commit()

        if (success) {
            Log.d(TAG, "Position saved to SharedPreferences successfully")
        } else {
            Log.e(TAG, "Failed to save position to SharedPreferences")
}

        // Now save to backup file
        val properties = Properties()
        properties.setProperty("last_book", bookCode)
        properties.setProperty("last_chapter", chapter.toString())

        try {
            // Ensure the parent directory exists
            backupFile.parentFile?.mkdirs()

            FileOutputStream(backupFile).use { output ->
                properties.store(output, "Bible Position Backup")
                Log.d(TAG, "Position saved to file: Book=$bookCode, Chapter=$chapter")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save position to file", e)
        }
    }

    /**
     * Call this method to force reload position data from file
     * Useful if you suspect the data isn't being loaded properly
     */
    fun forceReloadPosition(): Pair<String, Int> {
        restorePositionFromFile()
        return Pair(lastBookCode, lastChapter)
    }
}
