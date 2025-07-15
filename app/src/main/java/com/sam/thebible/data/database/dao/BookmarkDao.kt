package com.sam.thebible.data.database.dao

import androidx.room.*
import com.sam.thebible.data.model.Bookmark

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    suspend fun getAllBookmarks(): List<Bookmark>
    
    @Insert
    suspend fun insertBookmark(bookmark: Bookmark): Long
    
    @Update
    suspend fun updateBookmark(bookmark: Bookmark)
    
    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)
    
    @Query("SELECT * FROM bookmarks WHERE book = :book AND chapter = :chapter AND verse = :verse")
    suspend fun getBookmarkForVerse(book: String, chapter: Int, verse: Int): List<Bookmark>
}