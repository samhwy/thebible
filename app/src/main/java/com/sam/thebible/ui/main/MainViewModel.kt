package com.sam.thebible.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sam.thebible.data.model.Book
import com.sam.thebible.data.model.SearchResult
import com.sam.thebible.data.model.Verse
import com.sam.thebible.data.repository.BibleRepository
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

    private val _showEnglish = MutableLiveData<Boolean>()
    val showEnglish: LiveData<Boolean> = _showEnglish

    private val _isSearchMode = MutableLiveData<Boolean>()
    val isSearchMode: LiveData<Boolean> = _isSearchMode

    var lastBook: Book? = null
    var lastChapter: Int = 1

    init {
        _showEnglish.value = true
        _isSearchMode.value = false
        //loadBooks()
    }

    //private
    fun loadBooks(book: String, chapter: Int = 1) {
        viewModelScope.launch {
            try {
                
                val bookList = repository.getAllBooks()
                _books.value = bookList
                
                if (bookList.isNotEmpty()) {
                    var bookObj = bookList.firstOrNull { it.code == book }?: bookList.first()
                    Log.d("selectBook", "Checkpoint 1 load books: ${bookList.size}")
                    selectBook(bookObj, chapter)
                }
            } catch (e: Exception) {
                // Handle error
                println("Error loading books: ${e.message}")

            }
        }
    }


    fun selectBook(book: Book, chapter: Int = 1) {
        Log.d("selectBook", "checkpoint curr book: $lastBook, chapter: $lastChapter")
        //Thread.dumpStack()
        _currentBook.value = book
        _currentChapter.value = chapter
        if (lastBook!= book && lastChapter == 1) {  //check current book and chapter is the 1st chapter of last book
            loadChapter(book.code, book.numChapter ?: 1)
        }
        else {
            loadChapter(book.code, chapter)
        }
        lastBook = book
        lastChapter = chapter
    }

    fun selectChapter(chapter: Int) {
        _currentBook.value?.let { book ->
            if (chapter in 1..(book.numChapter ?: 0) ) {
                _currentChapter.value = chapter
                loadChapter(book.code, chapter)
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
                            selectBook(books[currentIndex + 1])
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
                            _currentBook.value = prevBook
                            _currentChapter.value = prevBook.numChapter ?: 1
                            loadChapter(prevBook.code, prevBook.numChapter ?: 1)
                        }
                    }
                }
            }
        }
    }

    private fun loadChapter(bookCode: String, chapter: Int) {
        viewModelScope.launch {
            try {
                val verseList = repository.getChapter(bookCode, chapter)
                _verses.value = verseList
                _isSearchMode.value = false
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

    fun toggleEnglish() {
        _showEnglish.value = !(_showEnglish.value ?: true)
    }

    fun exitSearchMode() {
        _isSearchMode.value = false
    }
    
    fun jumpToVerse(bookCode: String, chapter: Int) {
        _books.value?.find { it.code == bookCode }?.let { book ->
            selectBook(book, chapter)
            _isSearchMode.value = false
        }
    }
}