package com.sam.thebible.data.repository

import com.sam.thebible.data.database.dao.BookDao
import com.sam.thebible.data.database.dao.ChapterDao
import com.sam.thebible.data.database.dao.SearchDao
import com.sam.thebible.data.model.Book
import com.sam.thebible.data.model.SearchResult
import com.sam.thebible.data.model.Verse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BibleRepository @Inject constructor(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val searchDao: SearchDao
) {
    suspend fun getAllBooks(): List<Book> = bookDao.getAllBooks()
    
    suspend fun getChapter(bookCode: String, chapter: Int): List<Verse> {
        val chineseVerses = chapterDao.getChineseVerses(bookCode, chapter)
        val englishVerses = chapterDao.getEnglishVerses(bookCode, chapter)
        
        return chineseVerses.mapIndexed { index, chineseVerse ->
            val englishVerse = englishVerses.getOrNull(index)
            Verse(
                book = chineseVerse.book,
                chapter = chineseVerse.chapter,
                verse = chineseVerse.verse,
                chineseContent = chineseVerse.content,
                englishContent = englishVerse?.content ?: ""
            )
        }
    }
    
    suspend fun searchKeyword(keyword: String): List<SearchResult> {
        return searchDao.searchChinese(keyword)
    }
}