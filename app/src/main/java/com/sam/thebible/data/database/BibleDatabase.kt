package com.sam.thebible.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.sam.thebible.data.database.dao.BookDao
import com.sam.thebible.data.database.dao.ChapterDao
import com.sam.thebible.data.database.dao.SearchDao
import com.sam.thebible.data.model.Book
import com.sam.thebible.data.model.ChineseVerse
import com.sam.thebible.data.model.EnglishVerse

@Database(
    entities = [Book::class, ChineseVerse::class, EnglishVerse::class],
    version = 1,
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
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BibleDatabase::class.java,
                    "bible_database"
                ).createFromAsset("bible.db")
                    // .fallbackToDestructiveMigration()  // uncomment this if sqlite structure changed
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}