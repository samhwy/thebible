package com.sam.thebible.data.model

import android.annotation.SuppressLint
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey
    @ColumnInfo(name = "code")
    val code: String,
    @ColumnInfo(name = "num_chapter")
    val numChapter: Int?,
    @ColumnInfo(name = "eng_name")
    val engName: String?,
    @ColumnInfo(name = "tc_name")
    val tcName: String?,
    @ColumnInfo(name = "seq")
    val seq: Int?
)

@Entity(
    tableName = "hb5",
    primaryKeys = ["book", "chapter", "verse"] // Composite primary key
)
data class ChineseVerse(
    @ColumnInfo(name = "book")
    val book: String,
    @ColumnInfo(name = "chapter")
    val chapter: Int,
    @ColumnInfo(name = "verse")
    val verse: Int,
    @ColumnInfo(name = "content")
    val content: String
)


@Entity(
    tableName = "kjv",
    primaryKeys = ["book", "chapter", "verse"] // Composite primary key
)
data class EnglishVerse(
    @ColumnInfo(name = "book")
    val book: String,
    @ColumnInfo(name = "chapter")
    val chapter: Int,
    @ColumnInfo(name = "verse")
    val verse: Int,
    @ColumnInfo(name = "content")
    val content: String
)

data class Verse(
    val book: String,
    val chapter: Int,
    val verse: Int,
    val chineseContent: String,
    val englishContent: String
)

data class SearchResult(
    val book: String,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val content: String
)