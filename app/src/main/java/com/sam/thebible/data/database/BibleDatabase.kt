package com.sam.thebible.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.sam.thebible.data.database.dao.BookDao
import com.sam.thebible.data.database.dao.BookmarkDao
import com.sam.thebible.data.database.dao.ChapterDao
import com.sam.thebible.data.database.dao.SearchDao
import com.sam.thebible.data.model.Book
import com.sam.thebible.data.model.Bookmark
import com.sam.thebible.data.model.ChineseVerse
import com.sam.thebible.data.model.EnglishVerse

@Database(
    entities = [Book::class, ChineseVerse::class, EnglishVerse::class, Bookmark::class],
    version = 2,
    exportSchema = false
)
abstract class BibleDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun searchDao(): SearchDao

    companion object {
        @Volatile
        private var INSTANCE: BibleDatabase? = null

        fun getDatabase(context: Context): BibleDatabase {
            return INSTANCE ?: synchronized(this) {
                // Check if database already exists
                val dbFile = context.getDatabasePath("bible_database")
                val dbExists = dbFile.exists()

                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    BibleDatabase::class.java,
                    "bible_database"
                )

                // Only create from asset if database doesn't exist yet
                if (!dbExists) {
                    builder.createFromAsset("bible.db")
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                // Create bookmarks table if it doesn't exist
                                db.execSQL("""
                                    CREATE TABLE IF NOT EXISTS bookmarks (
                                        `id`	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                                        `book`	TEXT NOT NULL,
                                        `chapter`	INTEGER NOT NULL,
                                        `verse`	INTEGER NOT NULL,
                                        `selectedText`	TEXT NOT NULL,
                                        `notes`	TEXT DEFAULT '',
                                        `timestamp`	INTEGER NOT NULL DEFAULT (strftime('%s','now'))
                                    )
                                """.trimIndent())
                            }
                        })
                }

                val instance = builder
                     //.fallbackToDestructiveMigration() // Add this temporarily for testing()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    abstract fun bookmarkDao(): BookmarkDao
}