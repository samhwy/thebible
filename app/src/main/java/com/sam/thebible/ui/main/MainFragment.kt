package com.sam.thebible.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.thebible.adapter.VerseAdapter
import com.sam.thebible.databinding.FragmentMainBinding
import com.sam.thebible.data.model.Book
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by viewModels()
    private lateinit var verseAdapter: VerseAdapter

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
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        verseAdapter = VerseAdapter()
        binding.rvContent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = verseAdapter
        }
    }

    private fun setupObservers() {
        viewModel.books.observe(viewLifecycleOwner) { books ->
            setupBookSpinner(books)
        }
        
        viewModel.currentBook.observe(viewLifecycleOwner) { book ->
            book?.let {
                binding.spinnerBooks.setSelection(getBookPosition(it))
            }
        }
        
        viewModel.currentChapter.observe(viewLifecycleOwner) { chapter ->
            binding.tvCurrentChapter.text = chapter.toString()
        }
        
        viewModel.verses.observe(viewLifecycleOwner) { verses ->
            verseAdapter.submitList(verses)
        }
        
        viewModel.showEnglish.observe(viewLifecycleOwner) { showEnglish ->
            binding.cbShowEnglish.isChecked = showEnglish
            verseAdapter.setShowEnglish(showEnglish)
        }
    }

    private fun setupClickListeners() {
        binding.btnPrevChapter.setOnClickListener {
            viewModel.prevChapter()
        }
        
        binding.btnNextChapter.setOnClickListener {
            viewModel.nextChapter()
        }
        
        binding.btnSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString()
            viewModel.search(keyword)
        }
        
        binding.cbShowEnglish.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleEnglish()
        }
    }

    private fun setupBookSpinner(books: List<Book>) {
        val bookNames = books.map { "${it.tcName} (${it.engName})" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bookNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBooks.adapter = adapter
        
        binding.spinnerBooks.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.selectBook(books[position])
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun getBookPosition(book: Book): Int {
        return viewModel.books.value?.indexOf(book) ?: 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}