# Bible App (聖經)

An Android Bible reading app with Chinese (Major) and English text support.

## Features

- Chinese-English parallel reading. Can toggle English display.
- Offline bible reading
- Search functionality
- Dark mode. Allow selecting font size, colour and background colour.
- Remember last reading position
- allow swiping to next page 

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
   - `kjv` table: English verses (book, chapter, verse, content) with composite primary key

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

## Data Models

The app uses the following data models defined in `BibleModels.kt`:
- **Book**: Represents a Bible book with properties code, numChapter, engName, tcName, and seq
- **ChineseVerse (hb5)**: Represents a Chinese Bible verse with book, chapter, verse, and content
- **EnglishVerse (kjv)**: Represents an English Bible verse with book, chapter, verse, and content
- **Verse**: Combines both Chinese and English verses for display
- **SearchResult**: Represents a search result with book information and verse content