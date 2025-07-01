# Bible App (聖經)

An Android Bible reading app with Chinese and English text support.

## Features

- Browse Bible books and chapters
- Chinese-English parallel reading
- Search functionality
- Modern Android architecture (MVVM)
- Offline reading capability

## Architecture

- **MVVM Architecture** with ViewModel and LiveData
- **Room Database** for local data storage
- **Hilt** for dependency injection
- **Navigation Component** for fragment navigation
- **View Binding** for type-safe view access

## Setup

1. Place your `bible.db` SQLite database file in `app/src/main/assets/`
2. The database should contain:
   - `books` table: book information (code, numChapter, engName, tcName)
   - `hb5` table: Chinese verses (rowid, book, chapter, verse, content)
   - `kjv` table: English verses (rowid, book, chapter, verse, content)

## Database Schema

```sql
CREATE TABLE books (
    code TEXT PRIMARY KEY,
    numChapter INTEGER,
    engName TEXT,
    tcName TEXT
);

CREATE TABLE hb5 (
    rowid INTEGER PRIMARY KEY,
    book TEXT,
    chapter INTEGER,
    verse INTEGER,
    content TEXT
);

CREATE TABLE kjv (
    rowid INTEGER PRIMARY KEY,
    book TEXT,
    chapter INTEGER,
    verse INTEGER,
    content TEXT
);
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