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
        val chineseResults = searchDao.searchChinese(keyword).take(50)
        val englishResults = searchDao.searchEnglish(keyword).take(50)
        
        val combinedResults = mutableListOf<SearchResult>()
        combinedResults.addAll(chineseResults.map { it.copy(type = "chinese") })
        combinedResults.addAll(englishResults.map { it.copy(type = "english") })
        
        val sortedResults = combinedResults.sortedWith(
            compareBy<SearchResult> { it.book }
                .thenBy { it.chapter }
                .thenBy { it.verse }
        ).take(100)
        
        return if (chineseResults.size + englishResults.size >= 100) {
            sortedResults + SearchResult(
                book = "",
                bookName = "系統訊息",
                chapter = 0,
                verse = 0,
                content = "搜尋結果過多，僅顯示前100筆",
                type = "message"
            )
        } else sortedResults
    }
}