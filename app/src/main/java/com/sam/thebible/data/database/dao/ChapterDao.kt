package com.sam.thebible.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.sam.thebible.data.model.ChineseVerse
import com.sam.thebible.data.model.EnglishVerse

@Dao
interface ChapterDao {
    @Query("SELECT * FROM hb5 WHERE book = :bookCode AND chapter = :chapter ORDER BY verse")
    suspend fun getChineseVerses(bookCode: String, chapter: Int): List<ChineseVerse>
    
    @Query("SELECT * FROM asv WHERE book = :bookCode AND chapter = :chapter ORDER BY verse")
    suspend fun getEnglishVerses(bookCode: String, chapter: Int): List<EnglishVerse>
}