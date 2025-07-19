package com.sam.thebible.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sam.thebible.R
import com.sam.thebible.data.model.Bookmark

class BookmarkAdapter(
    private val onItemClick: (Bookmark) -> Unit,
    private val onItemLongClick: (Bookmark) -> Unit
) : ListAdapter<Bookmark, BookmarkAdapter.BookmarkViewHolder>(BookmarkDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bookmark, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val bookmark = getItem(position)
        holder.bind(bookmark)
    }

    inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(bookmark: Bookmark) {
            val tvBookChapterVerse = itemView.findViewById<TextView>(R.id.tvBookChapterVerse)
            val tvVerseContent = itemView.findViewById<TextView>(R.id.tvVerseContent)
            val tvNoteContent = itemView.findViewById<TextView>(R.id.tvNoteContent)

            tvBookChapterVerse.text = "${bookmark.book} ${bookmark.chapter}:${bookmark.verse}"
            tvVerseContent.text = bookmark.selectedText
            tvNoteContent.text = bookmark.notes ?: ""
            itemView.setOnClickListener { onItemClick(bookmark) }
            itemView.setOnLongClickListener {
                onItemLongClick(bookmark)
                true
            }
        }
    }

    companion object BookmarkDiffCallback : DiffUtil.ItemCallback<Bookmark>() {
        override fun areItemsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem == newItem
        }
    }
}