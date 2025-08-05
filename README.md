# Offline Bible App (離線繁體中英文聖經)

An Android offline Bible reading app with Chinese (Major) and English text support.

## Features

- **Bilingual Display**: Language modes - Chinese only, English only, or Chinese-English parallel reading
- **Bilingual Interface**: All menus, dialogs, and UI elements support both Chinese and English based on language setting
- **Advanced Text Selection**: Select specific text within verses for bookmarking with custom action menu (Add Bookmark, Copy, Select All)
- **Smart Bookmarking System**: Save selected text portions with personal notes, view/edit/delete bookmarks
- **Bookmark Management**: Export/import bookmarks to CSV files with file picker support
- **Enhanced Search**: Search through both Chinese and English verses with highlighted results
- **Improved Navigation**: 
  - Swipe left/right for chapter navigation with smooth animations
  - Jump to specific verses from search results and bookmarks
  - Remember last reading position across app sessions
- **Customizable Reading Experience**:
  - Dark/light mode toggle
  - Adjustable font size and colors
  - Multiple background color options
- **Offline Reading**: Complete offline Bible reading experience


## Architecture

- **MVVM Architecture** with ViewModel and LiveData
- **Room Database** for local data storage
- **Hilt** for dependency injection
- **Navigation Component** for fragment navigation
- **View Binding** for type-safe view access

## Setup

1. Place your `bible.db` SQLite database file in `app/src/main/assets/`
2. The database should contain:
   - `books` table: book information (code, numChapter, engName, tcName) with composite primary key
   - `hb5` table: Chinese verses (book, chapter, verse, content) with composite primary key
   - `kjv`/`asv` table: English verses (book, chapter, verse, content) with composite primary key

## Database Schema

```sql
CREATE TABLE "books" ( `code` TEXT NOT NULL, `eng_name` TEXT, `tc_name` TEXT, `num_chapter` INTEGER, `seq` INTEGER, PRIMARY KEY(`code`) )

CREATE TABLE `hb5` ( `book` TEXT NOT NULL, `chapter` INTEGER NOT NULL, `verse` INTEGER NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`book`,`chapter`,`verse`) )

CREATE TABLE `kjv` ( `book` TEXT NOT NULL, `chapter` INTEGER NOT NULL, `verse` INTEGER NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`book`,`chapter`,`verse`) )
```

## Build Requirements

- Android Studio Arctic Fox or later
- Kotlin 1.8+
- Android SDK 24+
- Gradle 8.0+

## Dependencies

- Room Database
- Hilt Dependency Injection
- Navigation Component
- Material Design Components
- AndroidX Lifecycle Components

## New Features Added

### Language System
- **Bilingual Support**: Chinese (中文), English, Chinese+English (中英對照) modes
- **Dynamic UI Translation**: All interface elements adapt to selected language
- **Smart Book Names**: Book titles display in appropriate language (e.g., "創世記" vs "Genesis")
- **English Bible version**: Control by modifying Database View to Bible version table (eg. KJV, ASV)

### Enhanced Bookmarking
- **Selective Text Bookmarking**: Choose specific text portions instead of entire verses
- **Rich Notes System**: Add personal notes to bookmarks with full editing capabilities
- **Export/Import**: Save bookmarks to CSV files and import from other devices
- **Bookmark Navigation**: Click bookmarks to jump directly to verses with precise scrolling

### Improved User Experience
- **Gesture Navigation**: Responsive swipe gestures for chapter navigation
- **Text Selection Menu**: Custom context menu with ordered options (Bookmark → Copy → Select All)
- **Precise Scrolling**: Accurate verse positioning when jumping from search results or bookmarks
- **File Management**: User-friendly file picker for bookmark export/import operations

## Data Models

The app uses the following data models defined in `BibleModels.kt`:
- **Book**: Represents a Bible book with properties code, numChapter, engName, tcName, and seq
- **ChineseVerse (hb5)**: Represents a Chinese Bible verse with book, chapter, verse, and content
- **EnglishVerse (english_version)**: Database view of English verses from kjv table
- **Verse**: Combines both Chinese and English verses for display
- **SearchResult**: Represents a search result with book information and verse content
- **Bookmark**: Stores user bookmarks with selected text, notes, and timestamps