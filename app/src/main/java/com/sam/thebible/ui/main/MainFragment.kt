package com.sam.thebible.ui.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.thebible.MainActivity
import com.sam.thebible.R
import com.sam.thebible.adapter.MainBookmarkAdapter
import com.sam.thebible.adapter.SearchResultAdapter
import com.sam.thebible.adapter.VerseAdapter
import com.sam.thebible.data.model.Bookmark
import com.sam.thebible.databinding.FragmentMainBinding
import com.sam.thebible.data.model.Book
import com.sam.thebible.data.model.Verse
import com.sam.thebible.utils.SettingsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import android.util.Log
import kotlin.collections.firstOrNull

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    val viewModel: MainViewModel by viewModels()
    private lateinit var verseAdapter: VerseAdapter
    private lateinit var searchResultAdapter: SearchResultAdapter
    private lateinit var bookmarkAdapter: MainBookmarkAdapter
    private lateinit var settingsManager: SettingsManager
    private lateinit var gestureDetector: GestureDetector
    private var currentFontSize = 16f
    private var currentFontColor = Color.BLACK
    private var currentBackgroundColor = Color.WHITE
    private var reloadPosition = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsManager = SettingsManager(requireContext())
        setupRecyclerView()
        setupGestureDetector()
        setupObservers()
        setupToolbarSpinners()
        loadSettings()
        val book = settingsManager.lastBookCode
        if (book.isNotEmpty()) {
            reloadPosition = true
            Log.d("MainActivity", "checkpoint saved book: $book saved chapter: ${settingsManager.lastChapter}")
            viewModel.loadBooks(book, settingsManager.lastChapter)
        } else viewModel.loadBooks("GEN", 1)
    }

    private fun loadSettings() {
        currentFontSize = 12f + settingsManager.fontSize * 2f
        val fontColors = arrayOf(R.color.white, R.color.black, R.color.green, R.color.yellow, R.color.orange)
        currentFontColor = getColorFromResource(fontColors.getOrElse(settingsManager.fontColorIndex) { R.color.black })
        val backgroundColors = arrayOf(R.color.white, R.color.black, R.color.parchment, R.color.dark_gray)
        currentBackgroundColor = getColorFromResource(backgroundColors.getOrElse(settingsManager.backgroundColorIndex) { R.color.white })
        viewModel.setLanguageMode(settingsManager.languageMode)
        applyTextSettings()
        applyFontSizeToSpinners()
        (activity as? MainActivity)?.applyFontSizeToMenus(currentFontSize)
    }

    private fun getColorFromResource(colorRes: Int): Int = requireContext().getColor(colorRes)

    private fun setupToolbarSpinners() {
        val mainActivity = activity as? MainActivity
        val (bookSpinner, chapterSpinner) = mainActivity?.getToolbarSpinners() ?: return
        bookSpinner?.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val book = viewModel.books.value?.get(position) ?: return
                val toChapter = when {
                    viewModel.lastBook != viewModel.currentBook.value && viewModel.lastChapter == 1 -> book.numChapter ?: 1
                    book.code != settingsManager.lastBookCode || (book.numChapter ?: 0) < settingsManager.lastChapter -> 1
                    else -> settingsManager.lastChapter
                }
                Log.d("MainActivity", "checkpoint current book: ${viewModel.currentBook.value} to Chapter:$toChapter ")
                if (!reloadPosition) viewModel.selectBook(book, toChapter)
                reloadPosition = false
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        chapterSpinner?.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) = viewModel.selectChapter(position + 1)
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        verseAdapter = VerseAdapter()
        searchResultAdapter = SearchResultAdapter(viewModel)
        bookmarkAdapter = MainBookmarkAdapter(viewModel)
        verseAdapter.setOnTextSelectedListener { verse, selectedText -> showBookmarkDialog(verse, selectedText) }
        bookmarkAdapter.setOnItemLongClickListener { bookmark -> showBookmarkOptionsDialog(bookmark) }
        binding.rvContent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = verseAdapter
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                if (abs(diffX) > abs(diffY) && abs(diffX) > 50 && abs(velocityX) > 50) {
                    if (diffX > 0) {
                        // Swipe right - previous chapter
                        onPrevChapter()
                    } else {
                        // Swipe left - next chapter
                        onNextChapter()
                    }
                    return true
                }
                return false
            }
        })

        binding.rvContent.setOnTouchListener { view, event ->
            val result = gestureDetector.onTouchEvent(event)
            if (!result) {
                view.performClick()
            }
            result
        }
    }

    private fun setupObservers() {
        viewModel.books.observe(viewLifecycleOwner) { books -> setupBookSpinner(books) }
        viewModel.currentBook.observe(viewLifecycleOwner) { book ->
            book?.let {
                Log.d("MainFragment", "checkpoint 3 current book: $it")
                if (!reloadPosition) settingsManager.lastBookCode = it.code
                val mainActivity = activity as? MainActivity
                val (bookSpinner, _) = mainActivity?.getToolbarSpinners() ?: return@let
                val position = if (!reloadPosition) getBookPosition(it) else viewModel.books.value?.firstOrNull { it.code == settingsManager.lastBookCode }?.let { getBookPosition(it) } ?: 0
                bookSpinner?.setSelection(position)
                setupChapterSpinner(it)
            }
        }
        viewModel.currentChapter.observe(viewLifecycleOwner) { chapter ->
            if (!reloadPosition) settingsManager.lastChapter = chapter
            val loadChapter = settingsManager.lastChapter
            Log.d("MainFragment", "checkpoint 4 current chapter: $loadChapter")
            val mainActivity = activity as? MainActivity
            val (_, chapterSpinner) = mainActivity?.getToolbarSpinners() ?: return@observe
            chapterSpinner?.adapter?.let { adapter ->
                val adapterCount = adapter.count
                when {
                    loadChapter <= adapterCount -> chapterSpinner.setSelection(loadChapter - 1)
                    adapterCount > 0 -> {
                        chapterSpinner.setSelection(adapterCount - 1)
                        settingsManager.lastChapter = adapterCount
                    }
                }
            }
        }
        viewModel.verses.observe(viewLifecycleOwner) { verses ->
            verseAdapter.submitList(verses)
            applyTextSettings()
            viewModel.targetVerse.value?.let { verse ->
                if (verse in 1..verses.size) {
                    binding.rvContent.post { (binding.rvContent.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(verse - 1, 0) }
                    viewModel.clearTargetVerse()
                }
            }
        }
        viewModel.languageMode.observe(viewLifecycleOwner) { languageMode ->
            verseAdapter.setLanguageMode(languageMode)
            viewModel.books.value?.let { books -> setupBookSpinner(books) }
        }
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchResultAdapter.submitList(results)
            searchResultAdapter.updateTextSettings(currentFontSize, currentFontColor)
        }
        viewModel.isSearchMode.observe(viewLifecycleOwner) { isSearchMode ->
            binding.rvContent.adapter = when {
                isSearchMode -> searchResultAdapter
                viewModel.isBookmarkMode.value == true -> bookmarkAdapter
                else -> verseAdapter
            }
        }
        viewModel.isBookmarkMode.observe(viewLifecycleOwner) { isBookmarkMode ->
            binding.rvContent.adapter = when {
                isBookmarkMode -> bookmarkAdapter
                viewModel.isSearchMode.value == true -> searchResultAdapter
                else -> verseAdapter
            }
        }
        viewModel.bookmarks.observe(viewLifecycleOwner) { bookmarks ->
            bookmarkAdapter.submitList(bookmarks)
            bookmarkAdapter.updateTextSettings(currentFontSize, currentFontColor)
        }
    }



    private fun setupBookSpinner(books: List<Book>) {
        val mainActivity = activity as? MainActivity
        val (bookSpinner, _) = mainActivity?.getToolbarSpinners() ?: return
        val bookNames = books.map { book ->
            when (settingsManager.languageMode) {
                1 -> book.engName ?: book.tcName ?: book.code
                else -> book.tcName ?: book.engName ?: book.code
            }
        }
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, bookNames) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View = super.getView(position, convertView, parent).apply { (this as? android.widget.TextView)?.textSize = currentFontSize - 2f }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View = super.getDropDownView(position, convertView, parent).apply { (this as? android.widget.TextView)?.textSize = currentFontSize }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bookSpinner?.adapter = adapter
        settingsManager.lastBookCode.takeIf { it.isNotEmpty() }?.let { lastBookCode ->
            books.find { it.code == lastBookCode }?.let { lastBook ->
                bookSpinner?.setSelection(books.indexOf(lastBook))
                if (!reloadPosition) viewModel.selectBook(lastBook, settingsManager.lastChapter)
            }
        }
    }

    private fun setupChapterSpinner(book: Book) {
        val mainActivity = activity as? MainActivity
        val (_, chapterSpinner) = mainActivity?.getToolbarSpinners() ?: return
        val chapterCount = book.numChapter ?: 1
        val isEnglish = settingsManager.languageMode == 1
        val chapters = (1..chapterCount).map { if (isEnglish) "$it" else "$it 章" }
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, chapters) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View = super.getView(position, convertView, parent).apply { (this as? android.widget.TextView)?.textSize = currentFontSize - 2f }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View = super.getDropDownView(position, convertView, parent).apply { (this as? android.widget.TextView)?.textSize = currentFontSize }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        chapterSpinner?.adapter = adapter
        if (book.code == settingsManager.lastBookCode) {
            val lastChapter = settingsManager.lastChapter
            when {
                lastChapter in 1..chapterCount -> chapterSpinner?.setSelection(lastChapter - 1).also { viewModel.selectChapter(lastChapter) }
                lastChapter > chapterCount && chapterCount > 0 -> {
                    chapterSpinner?.setSelection(chapterCount - 1)
                    viewModel.selectChapter(chapterCount)
                    settingsManager.lastChapter = chapterCount
                }
            }
        }
    }

    fun onPrevChapter() = addFlipAnimation(true).also { viewModel.prevChapter() }
    fun onNextChapter() = addFlipAnimation(false).also { viewModel.nextChapter() }

    private fun addFlipAnimation(isReverse: Boolean) {
        val screenWidth = binding.rvContent.width.toFloat()
        binding.rvContent.alpha = 1f
        binding.rvContent.translationX = if (isReverse) -screenWidth else screenWidth
        binding.rvContent.animate().translationX(0f).alpha(1f).setDuration(250).start()
    }
    fun onSearch(keyword: String) = searchResultAdapter.setSearchKeyword(keyword).also { viewModel.search(keyword) }
    fun exitSearchMode(): Boolean = viewModel.isSearchMode.value == true && viewModel.exitSearchMode().let { true }
    fun exitBookmarkMode(): Boolean = viewModel.isBookmarkMode.value == true && viewModel.exitBookmarkMode().let { true }
    fun loadBookmarks() = viewModel.loadBookmarks()
    fun navigateToVerse(book: String, chapter: Int, verse: Int) {
        settingsManager.lastBookCode = book
        settingsManager.lastChapter = chapter
        settingsManager.saveCurrentPosition(book, chapter)
        reloadPosition = true
        viewModel.jumpToVerse(book, chapter, verse)
    }


    fun applySettings(languageMode: Int, fontSize: Int, fontColorIndex: Int, backgroundColorIndex: Int) {
        viewModel.setLanguageMode(languageMode)
        currentFontSize = 12f + fontSize * 2f
        val fontColors = arrayOf(R.color.white, R.color.black, R.color.green, R.color.yellow, R.color.orange)
        currentFontColor = getColorFromResource(fontColors.getOrElse(fontColorIndex) { R.color.black })
        val backgroundColors = arrayOf(R.color.white, R.color.black, R.color.parchment, R.color.dark_gray)
        currentBackgroundColor = getColorFromResource(backgroundColors.getOrElse(backgroundColorIndex) { R.color.white })
        applyTextSettings()
        applyFontSizeToSpinners()
        (activity as? MainActivity)?.applyFontSizeToMenus(currentFontSize)
    }

    private fun applyTextSettings() {
        binding.rvContent.setBackgroundColor(currentBackgroundColor)
        verseAdapter.updateTextSettings(currentFontSize, currentFontColor)
        searchResultAdapter.updateTextSettings(currentFontSize, currentFontColor)
        bookmarkAdapter.updateTextSettings(currentFontSize, currentFontColor)
    }
    
    private fun applyFontSizeToSpinners() {
        viewModel.books.value?.let { books -> setupBookSpinner(books) }
        viewModel.currentBook.value?.let { book -> setupChapterSpinner(book) }
    }

    private fun getBookPosition(book: Book): Int = viewModel.books.value?.indexOf(book) ?: 0

    @SuppressLint("SetTextI18n")
    private fun showBookmarkDialog(verse: Verse, selectedText: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bookmark, null)
        val isEnglish = settingsManager.languageMode == 1
        val tvSelectedText = dialogView.findViewById<android.widget.TextView>(R.id.tvSelectedText)
        val etNotes = dialogView.findViewById<android.widget.EditText>(R.id.etNotes)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSave)
        etNotes.hint = if (isEnglish) "Add your notes here..." else "在此新增備註..."
        btnCancel.text = getString(if (isEnglish) R.string.cancel_en else R.string.cancel)
        btnSave.text = getString(if (isEnglish) R.string.save_en else R.string.save)
        val bookName = viewModel.books.value?.find { it.code == verse.book }?.let { book ->
            when (settingsManager.languageMode) {
                1 -> book.engName ?: book.tcName ?: book.code
                else -> book.tcName ?: book.engName ?: book.code
            }
        } ?: verse.book
        tvSelectedText.text = "$bookName ${verse.chapter}:${verse.verse} - $selectedText"
        val dialog = android.app.AlertDialog.Builder(requireContext()).setTitle(getString(if (isEnglish) R.string.add_bookmark_en else R.string.add_bookmark)).setView(dialogView).create()
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            viewModel.addBookmark(verse.book, verse.chapter, verse.verse, selectedText, etNotes.text.toString())
            dialog.dismiss()
        }
        dialog.show()
    }
    
    private fun showBookmarkOptionsDialog(bookmark: Bookmark) {
        val isEnglish = settingsManager.languageMode == 1
        val options = if (isEnglish) arrayOf(getString(R.string.view_notes_en), getString(R.string.edit_notes_en), getString(R.string.delete_bookmark_en)) else arrayOf(getString(R.string.view_notes), getString(R.string.edit_notes), getString(R.string.delete_bookmark))
        android.app.AlertDialog.Builder(requireContext()).setTitle(getString(if (isEnglish) R.string.bookmark_operations_en else R.string.bookmark_operations)).setItems(options) { _, which ->
            when (which) {
                0 -> viewNoteDialog(bookmark)
                1 -> showEditNoteDialog(bookmark)
                2 -> confirmDeleteBookmark(bookmark)
            }
        }.show()
    }
    
    private fun showEditNoteDialog(bookmark: Bookmark) {
        val editText = android.widget.EditText(requireContext()).apply { setText(bookmark.notes ?: ""); setSelection(text.length) }
        android.app.AlertDialog.Builder(requireContext()).setTitle("\uD83D\uDCDD").setView(editText).setNegativeButton(getString(R.string.cancel_en), null).setPositiveButton(getString(R.string.ok_en)) { _, _ ->
            editText.text.toString().takeIf { it != bookmark.notes }?.let { viewModel.updateBookmark(bookmark.copy(notes = it)) }
        }.create().show()
    }

    private fun confirmDeleteBookmark(bookmark: Bookmark) {
        val isEnglish = settingsManager.languageMode == 1
        android.app.AlertDialog.Builder(requireContext()).setTitle(if (isEnglish) "Delete Bookmark" else "删除書籤").setMessage(if (isEnglish) "Are you sure to delete this bookmark?" else "确定要删除該書籤嗎？").setNegativeButton(if (isEnglish) "Cancel" else "取消", null).setPositiveButton(if (isEnglish) "Delete" else "删除") { _, _ ->
            viewModel.deleteBookmark(bookmark)
            android.widget.Toast.makeText(requireContext(), if (isEnglish) "Bookmark deleted" else "書籤已删除", android.widget.Toast.LENGTH_SHORT).show()
        }.show()
    }

    private fun viewNoteDialog(bookmark: Bookmark) {
        android.app.AlertDialog.Builder(requireContext()).setTitle("\uD83D\uDCD6").setMessage(bookmark.notes).setPositiveButton("關閉", null).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}