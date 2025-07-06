package com.sam.thebible.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sam.thebible.data.model.SearchResult
import com.sam.thebible.databinding.ItemSearchResultBinding

class SearchResultAdapter : ListAdapter<SearchResult, SearchResultAdapter.ViewHolder>(DiffCallback()) {
    
    private var keyword: String = ""
    private var fontSize: Float = 16f
    private var fontColor: Int = Color.BLACK
    private var onItemClickListener: ((SearchResult) -> Unit)? = null
    
    fun setOnItemClickListener(listener: (SearchResult) -> Unit) {
        onItemClickListener = listener
    }

    fun setSearchKeyword(keyword: String) {
        this.keyword = keyword
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
        
        fun bind(result: SearchResult) {
            if (result.type == "message") {
                binding.tvBookChapter.text = result.bookName
                binding.tvContent.text = result.content
                binding.tvContent.setTextColor(Color.RED)
                return
            }

            binding.tvBookChapter.text = "${result.bookName} ${result.chapter}:${result.verse}"
            
            val highlightedContent = highlightKeyword(result.content, keyword)
            binding.tvContent.text = highlightedContent
            
            binding.tvBookChapter.textSize = fontSize - 2f
            binding.tvContent.textSize = fontSize
            binding.tvBookChapter.setTextColor(fontColor)
            binding.tvContent.setTextColor(fontColor)
            
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(result)
            }
        }
        
        private fun highlightKeyword(content: String, keyword: String): SpannableString {
            val spannable = SpannableString(content)
            if (keyword.isNotEmpty()) {
                var startIndex = 0
                while (true) {
                    val index = content.indexOf(keyword, startIndex, ignoreCase = true)
                    if (index == -1) break
                    
                    spannable.setSpan(
                        BackgroundColorSpan(Color.RED),
                        index,
                        index + keyword.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    startIndex = index + keyword.length
                }
            }
            return spannable
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem.book == newItem.book && 
                   oldItem.chapter == newItem.chapter && 
                   oldItem.verse == newItem.verse
        }

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem == newItem
        }
    }
}