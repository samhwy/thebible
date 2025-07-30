package com.sam.thebible.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.SkipQueryVerification
import com.sam.thebible.data.model.SearchResult

@Dao
interface SearchDao {
    @Query("""
        SELECT h.book, b.tc_name as bookName, h.chapter, h.verse, h.content, 'chinese' as type
        FROM hb5 h 
        JOIN books b ON h.book = b.code 
        WHERE h.content LIKE '%' || :keyword || '%' 
        ORDER BY b.seq, h.chapter, h.verse
        LIMIT 100
    """)
    suspend fun searchChinese(keyword: String): List<SearchResult>
    @SkipQueryVerification
    @Query("""
        SELECT k.book, b.tc_name as bookName, k.chapter, k.verse, k.content, 'english' as type
        FROM english_version k 
        JOIN books b ON k.book = b.code 
        WHERE k.content LIKE '%' || :keyword || '%' 
        ORDER BY b.seq, k.chapter, k.verse
        LIMIT 100
    """)
    suspend fun searchEnglish(keyword: String): List<SearchResult>
}