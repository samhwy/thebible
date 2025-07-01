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

    fun setShowEnglish(show: Boolean) {
        showEnglish = show
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseViewHolder {
        val binding = ItemVerseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VerseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VerseViewHolder, position: Int) {
        holder.bind(getItem(position), showEnglish)
    }

    class VerseViewHolder(private val binding: ItemVerseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(verse: Verse, showEnglish: Boolean) {
            binding.tvVerseNumber.text = verse.verse.toString()
            binding.tvChineseContent.text = verse.chineseContent
            
            if (showEnglish && verse.englishContent.isNotEmpty()) {
                binding.tvEnglishContent.text = verse.englishContent
                binding.tvEnglishContent.visibility = android.view.View.VISIBLE
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