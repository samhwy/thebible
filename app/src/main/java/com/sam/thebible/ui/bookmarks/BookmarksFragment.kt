package com.sam.thebible.ui.bookmarks

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.thebible.MainActivity
import com.sam.thebible.adapter.BookmarkAdapter
import com.sam.thebible.data.model.Bookmark
import com.sam.thebible.databinding.FragmentBookmarksBinding
import com.sam.thebible.ui.main.MainFragment // Assuming this is where navigateToVerse is
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookmarksFragment : Fragment() {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookmarksViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // BookmarkAdapter 需支持 onItemLongClick 回调
        val adapter = BookmarkAdapter(
            onItemClick = { bookmark: Bookmark ->
                // Handle bookmark click - navigate to the verse
                (activity as? MainActivity)?.let { mainActivity ->
                    (mainActivity.supportFragmentManager.findFragmentById(com.sam.thebible.R.id.nav_host_fragment_content_main) as? MainFragment)?.let { mainFragment ->
                        mainFragment.navigateToVerse(
                            bookmark.book,
                            bookmark.chapter,
                            bookmark.verse
                        )
                        Log.d("Bookmark clicked", bookmark.toString())
                        // Return to main fragment
                        parentFragmentManager.popBackStack()
                    }
                }
            },
            onItemLongClick = { bookmark: Bookmark ->
                showBookmarkOptionsDialog(bookmark)
            }
        )

        binding.rvBookmarks.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        lifecycleScope.launch {
            viewModel.bookmarks.collect { bookmarks ->
                if (bookmarks.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvBookmarks.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvBookmarks.visibility = View.VISIBLE
                    adapter.submitList(bookmarks)
                }
            }
        }
        
        // Set up close button
        binding.btnClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showBookmarkOptionsDialog(bookmark: Bookmark) {
        val options = arrayOf("檢視備註", "编緝備註", "删除書籤")
        AlertDialog.Builder(requireContext())
            .setTitle("操作書籤")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewNoteDialog(bookmark)
                    1 -> showEditNoteDialog(bookmark)
                    2 -> confirmDeleteBookmark(bookmark)
                }
            }
            .show()
    }

    private fun showEditNoteDialog(bookmark: Bookmark) {
        val editText = EditText(requireContext())
        editText.setText(bookmark.notes ?: "")
        AlertDialog.Builder(requireContext())
            .setTitle("編輯備註")
            .setView(editText)
            .setNegativeButton("取消", null)
            .setPositiveButton("保存") { _, _ ->
                val newNote = editText.text.toString()
                val updatedBookmark = bookmark.copy(notes = newNote)
                lifecycleScope.launch {
                    viewModel.updateBookmark(updatedBookmark)
                    Toast.makeText(requireContext(), "備注已保存", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun confirmDeleteBookmark(bookmark: Bookmark) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除書籤")
            .setMessage("确定要删除該書籤嗎？")
            .setNegativeButton("取消", null)
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.launch {
                    viewModel.deleteBookmark(bookmark)
                    Toast.makeText(requireContext(), "書籤已删除", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun viewNoteDialog(bookmark: Bookmark) {
        AlertDialog.Builder(requireContext())
            .setTitle("備註")
            .setMessage(bookmark.notes)
            .setPositiveButton("關閉", null)
            .show()
    }
}



private fun MainFragment.navigateToVerse(
    book: String,
    chapter: Int,
    verse: Int
) {
    // Use the viewModel to navigate to the specified verse
    viewModel.jumpToVerse(book, chapter, verse)
}
