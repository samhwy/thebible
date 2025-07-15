package com.sam.thebible.di

import android.content.Context
import com.sam.thebible.data.database.BibleDatabase
import com.sam.thebible.data.database.dao.BookDao
import com.sam.thebible.data.database.dao.BookmarkDao
import com.sam.thebible.data.database.dao.ChapterDao
import com.sam.thebible.data.database.dao.SearchDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BibleDatabase {
        return BibleDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideBookDao(database: BibleDatabase): BookDao = database.bookDao()
    
    @Provides
    fun provideChapterDao(database: BibleDatabase): ChapterDao = database.chapterDao()
    
    @Provides
    fun provideSearchDao(database: BibleDatabase): SearchDao = database.searchDao()
    
    @Provides
    fun provideBookmarkDao(database: BibleDatabase): BookmarkDao = database.bookmarkDao()
}