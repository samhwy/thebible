package com.sam.thebible.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sam.thebible.data.model.Verse
import com.sam.thebible.databinding.ItemVerseBinding

class VerseAdapter : ListAdapter<Verse, VerseAdapter.VerseViewHolder>(VerseDiffCallback()) {
    
    private var showEnglish = true
    private var fontSize = 16f
    private var textColor = android.graphics.Color.BLACK
    private var onTextSelectedListener: ((Verse, String) -> Unit)? = null
    
    fun setOnTextSelectedListener(listener: (Verse, String) -> Unit) {
        onTextSelectedListener = listener
    }

    fun setShowEnglish(show: Boolean) {
        showEnglish = show
        notifyDataSetChanged()
    }
    
    fun updateTextSettings(size: Float, color: Int) {
        fontSize = size
        textColor = color
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseViewHolder {
        val binding = ItemVerseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VerseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VerseViewHolder, position: Int) {
        holder.bind(getItem(position), showEnglish)
    }

    inner class VerseViewHolder(private val binding: ItemVerseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(verse: Verse, showEnglish: Boolean) {
            binding.tvVerseNumber.text = verse.verse.toString()
            binding.tvChineseContent.text = verse.chineseContent
            
            binding.tvVerseNumber.textSize = fontSize
            binding.tvChineseContent.textSize = fontSize
            binding.tvVerseNumber.setTextColor(textColor)
            binding.tvChineseContent.setTextColor(textColor)
            
            // Enable text selection
            binding.tvChineseContent.setTextIsSelectable(true)
            binding.tvChineseContent.setOnLongClickListener {
                onTextSelectedListener?.invoke(verse, binding.tvChineseContent.text.toString())
                true
            }
            
            if (showEnglish && verse.englishContent.isNotEmpty()) {
                binding.tvEnglishContent.text = verse.englishContent
                binding.tvEnglishContent.textSize = fontSize
                binding.tvEnglishContent.setTextColor(textColor)
                binding.tvEnglishContent.visibility = android.view.View.VISIBLE
                binding.tvEnglishContent.setTextIsSelectable(true)
                binding.tvEnglishContent.setOnLongClickListener {
                    onTextSelectedListener?.invoke(verse, binding.tvEnglishContent.text.toString())
                    true
                }
            } else {
                binding.tvEnglishContent.visibility = android.view.View.GONE
            }
        }
    }

    class VerseDiffCallback : DiffUtil.ItemCallback<Verse>() {
        override fun areItemsTheSame(oldItem: Verse, newItem: Verse): Boolean {
            return oldItem.book == newItem.book && 
                   oldItem.chapter == newItem.chapter && 
                   oldItem.verse == newItem.verse
        }

        override fun areContentsTheSame(oldItem: Verse, newItem: Verse): Boolean {
            return oldItem == newItem
        }
    }
}