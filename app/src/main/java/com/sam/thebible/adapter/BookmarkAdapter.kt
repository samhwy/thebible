package com.sam.thebible.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sam.thebible.R
import com.sam.thebible.data.model.Bookmark
import java.text.SimpleDateFormat
import java.util.*

class BookmarkAdapter : ListAdapter<Bookmark, RecyclerView.ViewHolder>(BookmarkDiffCallback) {
    private val VIEW_TYPE_BOOKMARK = 0
    private val VIEW_TYPE_CLOSE = 1

    private var onItemClickListener: ((Bookmark) -> Unit)? = null
    private var onItemLongClickListener: ((Bookmark) -> Unit)? = null
    private var onEditNoteClickListener: ((Bookmark) -> Unit)? = null
    private var onCloseClickListener: (() -> Unit)? = null

    fun setOnItemClickListener(listener: (Bookmark) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (Bookmark) -> Unit) {
        onItemLongClickListener = listener
    }

    fun setOnEditNoteClickListener(listener: (Bookmark) -> Unit) {
        onEditNoteClickListener = listener
    }
    
    fun setOnCloseClickListener(listener: () -> Unit) {
        onCloseClickListener = listener
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < currentList.size) VIEW_TYPE_BOOKMARK else VIEW_TYPE_CLOSE
    }
    
    override fun getItemCount(): Int {
        return super.getItemCount() + 1 // Add 1 for the close button
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_BOOKMARK -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_bookmark, parent, false)
                BookmarkViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_bookmark_close, parent, false)
                CloseViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BookmarkViewHolder -> {
                if (position < currentList.size) {
                    val bookmark = getItem(position)
                    holder.bind(bookmark)
                }
            }
            is CloseViewHolder -> {
                holder.bind()
            }
        }
    }

    inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(bookmark: Bookmark) {
            val tvBookChapterVerse = itemView.findViewById<TextView>(R.id.tvBookChapterVerse)
            val tvBookmarkDate = itemView.findViewById<TextView>(R.id.tvBookmarkDate)
            val tvVerseContent = itemView.findViewById<TextView>(R.id.tvVerseContent)
            val tvNoteContent = itemView.findViewById<TextView>(R.id.tvNoteContent)
            val ivNoteIcon = itemView.findViewById<ImageView>(R.id.ivNoteIcon)

            tvBookChapterVerse.text = "${bookmark.book} ${bookmark.chapter}:${bookmark.verse}"

            // 设置下划线和超链接颜色
            tvBookChapterVerse.paint.isUnderlineText = true

            // 格式化日期
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = bookmark.timestamp.let { dateFormat.format(Date(it*1000)) } ?: ""
            tvBookmarkDate.text = dateStr

            tvVerseContent.text = bookmark.selectedText
            tvNoteContent.text = bookmark.notes ?: ""

            // 整个item点击
            itemView.setOnClickListener {
                onItemClickListener?.invoke(bookmark)
            }
            // 长按
            itemView.setOnLongClickListener {
                onItemLongClickListener?.invoke(bookmark)
                true
            }
            // 书名章节点击（超链接）
            tvBookChapterVerse.setOnClickListener {
                onItemClickListener?.invoke(bookmark)
            }
            // 笔记编辑图标点击
            ivNoteIcon.setOnClickListener {
                onEditNoteClickListener?.invoke(bookmark)
            }
        }
    }

    inner class CloseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            val tvCloseBookmarks = itemView.findViewById<TextView>(R.id.tvCloseBookmarks)
            tvCloseBookmarks.setOnClickListener {
                onCloseClickListener?.invoke()
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