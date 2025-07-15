package com.sam.thebible.ui.main

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
import com.sam.thebible.adapter.SearchResultAdapter
import com.sam.thebible.adapter.VerseAdapter
import com.sam.thebible.databinding.FragmentMainBinding
import com.sam.thebible.data.model.Book
import com.sam.thebible.data.model.Verse
import com.sam.thebible.utils.SettingsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import android.util.Log

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    val viewModel: MainViewModel by viewModels()
    private lateinit var verseAdapter: VerseAdapter
    private lateinit var searchResultAdapter: SearchResultAdapter
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
            //  Reload the last saved position if the book is not empty
            reloadPosition = true
            Log.d("MainActivity", "checkpoint saved book: $book saved chapter: ${settingsManager.lastChapter}")
            viewModel.loadBooks(book, settingsManager.lastChapter)
        } else {
            viewModel.loadBooks("GEN", 1)
        }

    }

    private fun loadSettings() {
        currentFontSize = 12f + settingsManager.fontSize * 2f

        val fontColors = arrayOf(R.color.white, R.color.black, R.color.green, R.color.yellow, R.color.orange)
        currentFontColor = getColorFromResource(fontColors.getOrElse(settingsManager.fontColorIndex) { R.color.black })

        val backgroundColors = arrayOf(R.color.white, R.color.black, R.color.parchment, R.color.dark_gray)
        currentBackgroundColor = getColorFromResource(backgroundColors.getOrElse(settingsManager.backgroundColorIndex) { R.color.white })

        if (settingsManager.showEnglish != (viewModel.showEnglish.value ?: true)) {
            viewModel.toggleEnglish()
        }

        applyTextSettings()
    }

    private fun getColorFromResource(colorRes: Int): Int {
        return requireContext().getColor(colorRes)
    }

    private fun setupToolbarSpinners() {
        val mainActivity = activity as? MainActivity
        val (bookSpinner, chapterSpinner) = mainActivity?.getToolbarSpinners() ?: return

        bookSpinner?.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val book = viewModel.books.value?.get(position) ?: return
                val toChapter = if (viewModel.lastBook != viewModel.currentBook.value && viewModel.lastChapter == 1) {
                    book.numChapter ?: 1
                } else if (book.code != settingsManager.lastBookCode || book.numChapter?:0 < settingsManager.lastChapter)
                    1
                else
                    settingsManager.lastChapter

                Log.d("MainActivity", "checkpoint current book: ${viewModel.currentBook.value} to Chapter:$toChapter ")
                if (!reloadPosition)
                    viewModel.selectBook(book, toChapter)

                reloadPosition = false // Reset the flag after restoring the last saved position in onViewCreated
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        chapterSpinner?.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                viewModel.selectChapter(position + 1)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setupRecyclerView() {
        verseAdapter = VerseAdapter()
        searchResultAdapter = SearchResultAdapter()
        searchResultAdapter.setOnItemClickListener { searchResult ->
            viewModel.jumpToVerse(searchResult.book, searchResult.chapter)
        }
        verseAdapter.setOnTextSelectedListener { verse, selectedText ->
            showBookmarkDialog(verse, selectedText)
        }
        binding.rvContent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = verseAdapter
        }
    }

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

                if (abs(diffX) > abs(diffY) && abs(diffX) > 100 && abs(velocityX) > 100) {
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

        binding.rvContent.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun setupObservers() {
        viewModel.books.observe(viewLifecycleOwner) { books ->
            setupBookSpinner(books)
        }

        viewModel.currentBook.observe(viewLifecycleOwner) { book ->
            book?.let {
                settingsManager.lastBookCode = it.code
                val mainActivity = activity as? MainActivity
                val (bookSpinner, _) = mainActivity?.getToolbarSpinners() ?: return@let
                bookSpinner?.setSelection(getBookPosition(it))
                setupChapterSpinner(it)
            }
        }

        viewModel.currentChapter.observe(viewLifecycleOwner) { chapter ->
            Log.d("MainActivity", "checkpoint 4 current chapter: $chapter")
            //Thread.dumpStack()
            settingsManager.lastChapter = chapter
            val mainActivity = activity as? MainActivity
            val (_, chapterSpinner) = mainActivity?.getToolbarSpinners() ?: return@observe
            if (chapterSpinner?.adapter != null && chapter > 0) {
                chapterSpinner.setSelection(chapter - 1)
            }
        }

        viewModel.verses.observe(viewLifecycleOwner) { verses ->
            verseAdapter.submitList(verses)
            applyTextSettings()
        }

        viewModel.showEnglish.observe(viewLifecycleOwner) { showEnglish ->
            verseAdapter.setShowEnglish(showEnglish)
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchResultAdapter.submitList(results)
            searchResultAdapter.updateTextSettings(currentFontSize, currentFontColor)
        }

        viewModel.isSearchMode.observe(viewLifecycleOwner) { isSearchMode ->
            if (isSearchMode) {
                binding.rvContent.adapter = searchResultAdapter
            } else {
                binding.rvContent.adapter = verseAdapter
            }
        }
    }



    private fun setupBookSpinner(books: List<Book>) {
        val mainActivity = activity as? MainActivity
        val (bookSpinner, _) = mainActivity?.getToolbarSpinners() ?: return

        val bookNames = books.map { it.tcName ?: it.engName ?: it.code }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_custom, bookNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bookSpinner?.adapter = adapter

        // Load last book if available
        val lastBookCode = settingsManager.lastBookCode
        if (lastBookCode.isNotEmpty()) {
            val lastBook = books.find { it.code == lastBookCode }
            lastBook?.let {
                val position = books.indexOf(it)
                bookSpinner?.setSelection(position)
                val lastChapter = settingsManager.lastChapter
                if (!reloadPosition)
                   viewModel.selectBook(it, lastChapter)
            }
        }
    }

    private fun setupChapterSpinner(book: Book) {
        val mainActivity = activity as? MainActivity
        val (_, chapterSpinner) = mainActivity?.getToolbarSpinners() ?: return

        val chapterCount = book.numChapter ?: 1
        val chapters = (1..chapterCount).map { "$it 章" }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_custom, chapters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        chapterSpinner?.adapter = adapter

        // Load last chapter if same book
        if (book.code == settingsManager.lastBookCode) {
            val lastChapter = settingsManager.lastChapter
            if (lastChapter in 1..chapterCount) {
                chapterSpinner?.setSelection(lastChapter - 1)
                viewModel.selectChapter(lastChapter)
            }
        }
    }

    fun onPrevChapter() {
        addFlipAnimation(true)
        viewModel.prevChapter()
    }

    fun onNextChapter() {
        addFlipAnimation(false)
        viewModel.nextChapter()
    }
    
    private fun addFlipAnimation(isReverse: Boolean) {
        val pivotX = if (isReverse) binding.rvContent.width.toFloat() else 0f
        binding.rvContent.pivotX = pivotX
        binding.rvContent.pivotY = binding.rvContent.height / 2f
        
        binding.rvContent.animate()
            .rotationY(if (isReverse) 90f else -90f)
            .scaleX(0.8f)
            .alpha(0.7f)
            .setDuration(200)
            .withEndAction {
                binding.rvContent.rotationY = if (isReverse) -90f else 90f
                binding.rvContent.animate()
                    .rotationY(0f)
                    .scaleX(1f)
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }

    fun onSearch(keyword: String) {
        searchResultAdapter.setSearchKeyword(keyword)
        viewModel.search(keyword)
    }

    fun exitSearchMode(): Boolean {
        return if (viewModel.isSearchMode.value == true) {
            viewModel.exitSearchMode()
            true
        } else {
            false
        }
    }


    fun applySettings(showEnglish: Boolean, fontSize: Int, fontColorIndex: Int, backgroundColorIndex: Int) {
        if (showEnglish != (viewModel.showEnglish.value ?: true)) {
            viewModel.toggleEnglish()
        }

        currentFontSize = 12f + fontSize * 2f

        val fontColors = arrayOf(R.color.white, R.color.black, R.color.green, R.color.yellow, R.color.orange)
        currentFontColor = getColorFromResource(fontColors.getOrElse(fontColorIndex) { R.color.black })

        val backgroundColors = arrayOf(R.color.white, R.color.black, R.color.parchment, R.color.dark_gray)
        currentBackgroundColor = getColorFromResource(backgroundColors.getOrElse(backgroundColorIndex) { R.color.white })

        applyTextSettings()
    }

    private fun applyTextSettings() {
        binding.rvContent.setBackgroundColor(currentBackgroundColor)
        verseAdapter.updateTextSettings(currentFontSize, currentFontColor)
        searchResultAdapter.updateTextSettings(currentFontSize, currentFontColor)
    }

    private fun getBookPosition(book: Book): Int {
        return viewModel.books.value?.indexOf(book) ?: 0
    }

    private fun showBookmarkDialog(verse: Verse, selectedText: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bookmark, null)
        val tvSelectedText = dialogView.findViewById<android.widget.TextView>(R.id.tvSelectedText)
        val etNotes = dialogView.findViewById<android.widget.EditText>(R.id.etNotes)
        
        tvSelectedText.text = "${verse.book} ${verse.chapter}:${verse.verse} - $selectedText"
        
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialogView.findViewById<android.view.View>(R.id.btnCopy).setOnClickListener {
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Bible Verse", tvSelectedText.text)
            clipboard.setPrimaryClip(clip)
            android.widget.Toast.makeText(requireContext(), "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        dialogView.findViewById<android.view.View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<android.view.View>(R.id.btnSave).setOnClickListener {
            val notes = etNotes.text.toString()
            viewModel.addBookmark(verse.book, verse.chapter, verse.verse, selectedText, notes)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}