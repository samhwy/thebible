package com.sam.thebible.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.sam.thebible.data.model.Book

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY seq")
    suspend fun getAllBooks(): List<Book>
    
    @Query("SELECT * FROM books WHERE code = :bookCode")
    suspend fun getBook(bookCode: String): Book?
}