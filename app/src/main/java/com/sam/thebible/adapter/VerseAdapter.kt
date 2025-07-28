package com.sam.thebible.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sam.thebible.data.model.Verse
import com.sam.thebible.databinding.ItemVerseBinding
import com.sam.thebible.R
import com.sam.thebible.utils.SettingsManager

class VerseAdapter : ListAdapter<Verse, VerseAdapter.VerseViewHolder>(VerseDiffCallback()) {
    
    private var languageMode = 2 // 0=Chinese, 1=English, 2=Both
    private var fontSize = 16f
    private var textColor = android.graphics.Color.BLACK
    private var onTextSelectedListener: ((Verse, String) -> Unit)? = null
    private lateinit var settingsManager: SettingsManager

    fun setOnTextSelectedListener(listener: (Verse, String) -> Unit) {
        onTextSelectedListener = listener
    }

    fun setLanguageMode(mode: Int) {
        languageMode = mode
        notifyDataSetChanged()
    }
    
    fun updateTextSettings(size: Float, color: Int) {
        fontSize = size
        textColor = color
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseViewHolder {
        val binding = ItemVerseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        settingsManager = SettingsManager(parent.context)
        return VerseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VerseViewHolder, position: Int) {
        holder.bind(getItem(position), languageMode)
    }

    inner class VerseViewHolder(private val binding: ItemVerseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(verse: Verse, languageMode: Int) {
            binding.tvVerseNumber.text = verse.verse.toString()
            binding.tvVerseNumber.textSize = fontSize
            binding.tvVerseNumber.setTextColor(textColor)
            
            // Show/hide Chinese content based on language mode
            if (languageMode == 0 || languageMode == 2) { // Chinese or Both
                binding.tvChineseContent.text = verse.chineseContent
                binding.tvChineseContent.textSize = fontSize
                binding.tvChineseContent.setTextColor(textColor)
                binding.tvChineseContent.visibility = android.view.View.VISIBLE
                binding.tvChineseContent.setTextIsSelectable(true)
                binding.tvChineseContent.customSelectionActionModeCallback = createSelectionCallback(verse, binding.tvChineseContent)
            } else {
                binding.tvChineseContent.visibility = android.view.View.GONE
            }
            
            // Show/hide English content based on language mode
            if ((languageMode == 1 || languageMode == 2) && verse.englishContent.isNotEmpty()) { // English or Both
                binding.tvEnglishContent.text = verse.englishContent
                binding.tvEnglishContent.textSize = fontSize
                binding.tvEnglishContent.setTextColor(textColor)
                binding.tvEnglishContent.visibility = android.view.View.VISIBLE
                binding.tvEnglishContent.setTextIsSelectable(true)
                binding.tvEnglishContent.customSelectionActionModeCallback = createSelectionCallback(verse, binding.tvEnglishContent)
            } else {
                binding.tvEnglishContent.visibility = android.view.View.GONE
            }
        }
    }

    private fun createSelectionCallback(verse: Verse, textView: android.widget.TextView): android.view.ActionMode.Callback {
        return object : android.view.ActionMode.Callback {
            override fun onCreateActionMode(mode: android.view.ActionMode?, menu: android.view.Menu?): Boolean {
                val context = textView.context
                val isEnglish = settingsManager.languageMode == 1
                
                menu?.clear()
                // 1. Add Bookmark (first)
                menu?.clear()
                menu?.add(0, 1, 1, context.getString(if (languageMode == 1) R.string.add_bookmark_en else R.string.add_bookmark))
                // 2. Copy (second)
                menu?.add(0, 2, 2, context.getString(if (languageMode == 1) R.string.copy_en else R.string.copy))
                // 3. Select All (third)
                menu?.add(0, 3, 3, context.getString(if (languageMode == 1) R.string.select_all_en else R.string.select_all))
                
                return true
            }
            
            override fun onActionItemClicked(mode: android.view.ActionMode?, item: android.view.MenuItem?): Boolean {
                when (item?.itemId) {
                    1 -> { // Add Bookmark
                        val selectedText = getSelectedText(textView)
                        onTextSelectedListener?.invoke(verse, selectedText)
                        mode?.finish()
                        return true
                    }
                    2 -> { // Copy
                        val selectedText = getSelectedText(textView)
                        val clipboard = textView.context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Bible Verse", selectedText)
                        clipboard.setPrimaryClip(clip)
                        mode?.finish()
                        return true
                    }
                    3 -> { // Select All
                        textView.selectAllText()
                        return true
                    }
                }
                return false
            }
            
            override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: android.view.Menu?) = false
            override fun onDestroyActionMode(mode: android.view.ActionMode?) {}
        }
    }
    
    private fun getSelectedText(textView: android.widget.TextView): String {
        val start = textView.selectionStart
        val end = textView.selectionEnd
        return if (start >= 0 && end > start) {
            textView.text.substring(start, end)
        } else {
            textView.text.toString()
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

// New extension function for TextView
fun android.widget.TextView.selectAllText() {
    val content = this.text
    if (content is android.text.Spannable) {
        val selectionStart = 0
        val selectionEnd = content.length
        android.text.Selection.setSelection(content, selectionStart, selectionEnd)
    }
}
