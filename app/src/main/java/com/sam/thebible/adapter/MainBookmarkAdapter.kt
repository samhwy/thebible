package com.sam.thebible.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sam.thebible.data.model.Bookmark
import com.sam.thebible.databinding.ItemSearchResultBinding
import java.text.SimpleDateFormat
import java.util.*

class MainBookmarkAdapter : ListAdapter<Bookmark, MainBookmarkAdapter.ViewHolder>(DiffCallback()) {
    
    private var fontSize: Float = 16f
    private var fontColor: Int = Color.BLACK
    private var onItemClickListener: ((Bookmark) -> Unit)? = null
    private var onItemLongClickListener: ((Bookmark) -> Unit)? = null
    
    fun setOnItemClickListener(listener: (Bookmark) -> Unit) {
        onItemClickListener = listener
    }
    
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
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSearchResultBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(bookmark: Bookmark) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = bookmark.timestamp.let { dateFormat.format(Date(it*1000)) } ?: ""

            binding.tvBookChapter.text = "${bookmark.book} ${bookmark.chapter}:${bookmark.verse} $dateStr"
            
            val content = bookmark.selectedText
            val notes = bookmark.notes

            // Format the content with notes if available
            val displayText = if (!notes.isNullOrEmpty()) {
                "$content\n備註: $notes"
            } else {
                content
            }
            
            binding.tvContent.text = displayText
            
            binding.tvBookChapter.textSize = fontSize - 2f
            binding.tvContent.textSize = fontSize
            binding.tvBookChapter.setTextColor(fontColor)
            binding.tvContent.setTextColor(fontColor)
            
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(bookmark)
            }
            
            binding.root.setOnLongClickListener {
                onItemLongClickListener?.invoke(bookmark)
                true
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