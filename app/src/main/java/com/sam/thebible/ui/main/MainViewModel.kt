package com.sam.thebible.ui.main

import android.R
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sam.thebible.data.model.Book
import com.sam.thebible.data.model.SearchResult
import com.sam.thebible.data.model.Verse
import com.sam.thebible.data.repository.BibleRepository
import com.sam.thebible.utils.Constants.DEFAULT_BOOK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: BibleRepository
) : ViewModel() {

    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> = _books

    private val _currentBook = MutableLiveData<Book>()
    val currentBook: LiveData<Book> = _currentBook

    private val _currentChapter = MutableLiveData<Int>()
    val currentChapter: LiveData<Int> = _currentChapter

    private val _verses = MutableLiveData<List<Verse>>()
    val verses: LiveData<List<Verse>> = _verses

    private val _searchResults = MutableLiveData<List<SearchResult>>()
    val searchResults: LiveData<List<SearchResult>> = _searchResults

    private val _languageMode = MutableLiveData<Int>()
    val languageMode: LiveData<Int> = _languageMode

    private val _isSearchMode = MutableLiveData<Boolean>()
    val isSearchMode: LiveData<Boolean> = _isSearchMode
    
    private val _isBookmarkMode = MutableLiveData<Boolean>()
    val isBookmarkMode: LiveData<Boolean> = _isBookmarkMode
    
    private val _bookmarks = MutableLiveData<List<com.sam.thebible.data.model.Bookmark>>()
    val bookmarks: LiveData<List<com.sam.thebible.data.model.Bookmark>> = _bookmarks
    
    private val _targetVerse = MutableLiveData<Int?>()
    val targetVerse: MutableLiveData<Int?> = _targetVerse

    var lastBook: Book? = null
    var lastChapter: Int = 1

    init {
        _languageMode.value = 2 // Default to Both
        _isSearchMode.value = false
        _isBookmarkMode.value = false
        //loadBooks()
    }

    /**
     * Load books from database and update UI
     * @param book default is DEFAULT_BOOK
     * @param chapter default is 1
     */
    fun loadBooks(book: String = DEFAULT_BOOK, chapter: Int = 1) {
        viewModelScope.launch {
            try {
                
                val bookList = repository.getAllBooks()
                _books.value = bookList
                
                if (bookList.isNotEmpty()) {
                    var bookObj = bookList.firstOrNull { it.code == book }?: bookList.first()
                    Log.d("selectBook", "Checkpoint 1 load books: ${bookList.size}")
                    // Thread.dumpStack()
                    selectBook(bookObj, chapter)
                }
            } catch (e: Exception) {
                // Handle error
                println("Error loading books: ${e.message}")

            }
        }
    }


    /**
     * go to the book and chapter when user select the book from UI
     * @param book
     * @param chapter default is 1
     */
    fun selectBook(book: Book, chapter: Int = 1, verse: Int? = 1) {
        Log.d("selectBook", "checkpoint curr book: $lastBook, chapter: $lastChapter")
        //Thread.dumpStack()
        _currentBook.value = book
        _currentChapter.value = chapter
        if (lastBook!= book && lastChapter == 1) {  //check current book and chapter is the 1st chapter of last book
            loadChapter(book.code, book.numChapter ?: 1, verse)
        }
        else {
            loadChapter(book.code, chapter,  verse)
        }
        lastBook = book
        lastChapter = chapter
    }

    fun selectChapter(chapter: Int) {
        _currentBook.value?.let { book ->
            if (chapter in 1..(book.numChapter ?: 0) ) {
                _currentChapter.value = chapter
                loadChapter(book.code, chapter)
                lastBook = book
                lastChapter = chapter
            }
        }
    }

    fun nextChapter() {
        _currentBook.value?.let { book ->
            _currentChapter.value?.let { chapter ->
                if (chapter < (book.numChapter ?: 0)) {
                    selectChapter(chapter + 1)
                } else {
                    // Go to next book's first chapter
                    _books.value?.let { books ->
                        val currentIndex = books.indexOf(book)
                        if (currentIndex < books.size - 1) {
                            selectBook(books[currentIndex + 1], 1)
                        }
                    }
                }
            }
        }
    }

    fun prevChapter() {
        _currentBook.value?.let { book ->
            _currentChapter.value?.let { chapter ->
                if (chapter > 1) {
                    selectChapter(chapter - 1)
                } else {
                    // Go to previous book's last chapter
                    _books.value?.let { books ->
                        val currentIndex = books.indexOf(book)
                        if (currentIndex > 0) {
                            val prevBook = books[currentIndex - 1]
                            val lastChapterNum = prevBook.numChapter ?: 1
                            selectBook(prevBook, lastChapterNum)
                        }
                    }
                }
            }
        }
    }


    /**
     * Load chapter from database and update UI with verseList
     * @param bookCode
     * @param chapter
     */
    fun loadChapter(bookCode: String, chapter: Int, verse: Int? = 1) {
        viewModelScope.launch {
            try {
                Log.d("loadChapter", "loadChapter book: $bookCode, chapter: $chapter, verse: $verse")
                val verseList = repository.getChapter(bookCode, chapter)
                _verses.value = verseList
                _isSearchMode.value = false
                _isBookmarkMode.value = false
                if (verse != null && verse > 1)
                  _targetVerse.value = verse
            } catch (e: Exception) {
                // Handle error
                println("Error loading chapter: ${e.message}")
            }
        }
    }

    fun search(keyword: String) {
        if (keyword.isBlank()) return
        
        viewModelScope.launch {
            try {
                val results = repository.searchKeyword(keyword)
                _searchResults.value = results
                _isSearchMode.value = true
            } catch (e: Exception) {
                // Handle error
                println("Error searching: ${e.message}")
            }
        }
    }

    fun setLanguageMode(mode: Int) {
        _languageMode.value = mode
    }

    fun exitSearchMode() {
        _isSearchMode.value = false
    }
    
    fun exitBookmarkMode() {
        _isBookmarkMode.value = false
    }
    
    fun clearTargetVerse() {
        _targetVerse.value = null
    }
    
    fun jumpToVerse(bookCode: String, chapter: Int, verse: Int = 1) {

        _books.value?.find { it.code == bookCode }?.let { book ->
            Log.d("jumpToVerse", "checkpoint 5 book: $bookCode, chapter: $chapter, verse: $verse")
            selectBook(book, chapter, verse)
            _isSearchMode.value = false
            _isBookmarkMode.value = false
            // _targetVerse.value = verse
        }
    }

    fun backToLastBkChapter() {
        val bookCode = lastBook?.code ?: _currentBook.value?.code ?: DEFAULT_BOOK
        val chapter = if (lastChapter > 0) lastChapter else _currentChapter.value ?: 1
        
        _currentBook.value = lastBook ?: _currentBook.value
        _currentChapter.value = chapter
        loadChapter(bookCode, chapter)
    }

    fun addBookmark(book: String, chapter: Int, verse: Int, selectedText: String, notes: String) {
        viewModelScope.launch {
            try {
                val bookmark = com.sam.thebible.data.model.Bookmark(
                    book = book,
                    chapter = chapter,
                    verse = verse,
                    selectedText = selectedText,
                    notes = notes
                )
                repository.addBookmark(bookmark)
            } catch (e: Exception) {
                println("Error adding bookmark: ${e.message}")
            }
        }
    }
    
    fun loadBookmarks() {
        viewModelScope.launch {
            try {
                val bookmarksList = repository.getAllBookmarks()
                _bookmarks.value = bookmarksList
                _isBookmarkMode.value = true
                _isSearchMode.value = false
            } catch (e: Exception) {
                println("Error loading bookmarks: ${e.message}")
            }
        }
    }
    
    fun updateBookmark(bookmark: com.sam.thebible.data.model.Bookmark) {
        viewModelScope.launch {
            try {
                repository.updateBookmark(bookmark)
                // Refresh the bookmarks list
                loadBookmarks()
            } catch (e: Exception) {
                println("Error updating bookmark: ${e.message}")
            }
        }
    }
    
    fun deleteBookmark(bookmark: com.sam.thebible.data.model.Bookmark) {
        viewModelScope.launch {
            try {
                repository.deleteBookmark(bookmark)
                // Refresh the bookmarks list
                loadBookmarks()
            } catch (e: Exception) {
                println("Error deleting bookmark: ${e.message}")
            }
        }
    }
    
    suspend fun exportBookmarks(): String {
        return try {
            val bookmarks = repository.getAllBookmarks()
            val csv = StringBuilder()
            csv.append("Book,Chapter,Verse,SelectedText,Notes,Timestamp\n")
            bookmarks.forEach { bookmark ->
                csv.append("\"${bookmark.book}\",${bookmark.chapter},${bookmark.verse},\"${bookmark.selectedText.replace("\"", "\\\"")}\",\"${bookmark.notes?.replace("\"", "\\\"") ?: ""}\",${bookmark.timestamp}\n")
            }
            csv.toString()
        } catch (e: Exception) {
            throw Exception("Export failed: ${e.message}")
        }
    }
    
    suspend fun importBookmarks(csvContent: String) {
        try {
            val lines = csvContent.split("\n").drop(1).filter { it.isNotBlank() }
            val bookmarks = lines.mapNotNull { line ->
                try {
                    val parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                    if (parts.size >= 6) {
                        com.sam.thebible.data.model.Bookmark(
                            book = parts[0].trim('\"'),
                            chapter = parts[1].toInt(),
                            verse = parts[2].toInt(),
                            selectedText = parts[3].trim('\"').replace("\\\"", "\""),
                            notes = parts[4].trim('\"').replace("\\\"", "\""),
                            timestamp = parts[5].toLong()
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            repository.insertBookmarks(bookmarks)
        } catch (e: Exception) {
            throw Exception("Import failed: ${e.message}")
        }
    }
}