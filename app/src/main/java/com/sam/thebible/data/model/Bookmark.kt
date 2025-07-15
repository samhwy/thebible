package com.sam.thebible.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    
    @ColumnInfo(name = "book")
    val book: String,

    @ColumnInfo(name = "chapter")
    val chapter: Int,

    @ColumnInfo(name = "verse")
    val verse: Int,

    @ColumnInfo(name = "selectedText")
    val selectedText: String,

    @ColumnInfo(name = "notes")
    val notes: String? = "",

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis() / 1000
)