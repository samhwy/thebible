package com.sam.thebible.ui.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sam.thebible.data.model.Bookmark
import com.sam.thebible.data.repository.BibleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val repository: BibleRepository
) : ViewModel() {

    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    init {
        loadBookmarks()
    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            _bookmarks.value = repository.getAllBookmarks()
        }
    }

    suspend fun updateBookmark(bookmark: Bookmark) {
        repository.updateBookmark(bookmark)
    }

    suspend fun deleteBookmark(bookmark: Bookmark) {
        repository.deleteBookmark(bookmark)
    }
}