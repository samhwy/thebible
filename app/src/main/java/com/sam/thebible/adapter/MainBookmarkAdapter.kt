package com.sam.thebible.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sam.thebible.data.model.Bookmark
import com.sam.thebible.databinding.ItemSearchResultBinding
import com.sam.thebible.ui.main.MainViewModel
import com.sam.thebible.utils.SettingsManager
import java.text.SimpleDateFormat
import java.util.*

class MainBookmarkAdapter(private val viewModel: MainViewModel) : ListAdapter<Bookmark, MainBookmarkAdapter.ViewHolder>(DiffCallback()) {
    private var fontSize: Float = 16f
    private var fontColor: Int = Color.BLACK
    private var onItemLongClickListener: ((Bookmark) -> Unit)? = null
    private lateinit var settingsManager: SettingsManager

    fun setOnItemLongClickListener(listener: (Bookmark) -> Unit) {
        onItemLongClickListener = listener
    }

    fun updateTextSettings(fontSize: Float, fontColor: Int) {
        this.fontSize = fontSize
        this.fontColor = fontColor
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        settingsManager = SettingsManager(parent.context)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position),  position == itemCount - 1)
    }

    inner class ViewHolder(private val binding: ItemSearchResultBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(bookmark: Bookmark, isLastItem: Boolean) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = bookmark.timestamp.let { dateFormat.format(Date(it*1000)) } ?: ""

            val bookName = viewModel.books.value?.find { it.code == bookmark.book }?.let { book ->
                when (settingsManager.languageMode) {
                    1 -> book.engName ?: book.tcName ?: book.code // English mode
                    else -> book.tcName ?: book.engName ?: book.code // Chinese/Both modes
                }
            } ?: bookmark.book
            
            binding.tvBookChapter.text = "$bookName ${bookmark.chapter}:${bookmark.verse} ($dateStr)"

            binding.tvContent.text = bookmark.selectedText
            binding.tvNotes.text = "\uD83D\uDCDD: ${bookmark.notes}"

            binding.tvBookChapter.textSize = fontSize - 2f
            binding.tvContent.textSize = fontSize -3f
            binding.tvNotes.textSize = fontSize -3f
            binding.tvBookChapter.setTextColor(fontColor)
            binding.tvContent.setTextColor(fontColor)
            binding.tvNotes.setTextColor(fontColor)
            binding.tvContent.maxLines = 1
            binding.tvContent.ellipsize = android.text.TextUtils.TruncateAt.END

            binding.root.setOnClickListener {
                Log.d("MainBookmarkAdapter", " bookmark click=${bookmark.book}, ${bookmark.chapter}, ${bookmark.verse}")
                viewModel.jumpToVerse(bookmark.book, bookmark.chapter, bookmark.verse)
            }

            binding.root.setOnLongClickListener {
                onItemLongClickListener?.invoke(bookmark)
                true
            }

            // Show close button only on the last item
            binding.tvClose.visibility = if (isLastItem || itemCount == 0) android.view.View.VISIBLE else android.view.View.GONE

            // Close button handling - return to main page with last position
            binding.tvClose.setOnClickListener {
                viewModel.backToLastBkChapter()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Bookmark>() {
        override fun areItemsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem == newItem
        }
    }
}