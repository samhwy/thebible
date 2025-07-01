package com.sam.thebible.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.sam.thebible.data.model.SearchResult

@Dao
interface SearchDao {
    @Query("""
        SELECT h.book, b.tc_name as bookName, h.chapter, h.verse, h.content 
        FROM hb5 h 
        JOIN books b ON h.book = b.code 
        WHERE h.content LIKE '%' || :keyword || '%' 
        ORDER BY h.book, h.chapter, h.verse
        LIMIT 100
    """)
    suspend fun searchChinese(keyword: String): List<SearchResult>
    
    @Query("""
        SELECT k.book, b.eng_name as bookName, k.chapter, k.verse, k.content 
        FROM kjv k 
        JOIN books b ON k.book = b.code 
        WHERE k.content LIKE '%' || :keyword || '%' 
        ORDER BY k.book, k.chapter, k.verse
        LIMIT 100
    """)
    suspend fun searchEnglish(keyword: String): List<SearchResult>
}