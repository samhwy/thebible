Place your bible.db SQLite database file in this assets folder.

The database should contain the following tables:
1. books - with columns: code, numChapter, engName, tcName
2. hb5 - Chinese verses with columns: rowid, book, chapter, verse, content
3. kjv - English verses with columns: rowid, book, chapter, verse, content

Example structure:
- books table: Contains book information like Genesis (GEN), Exodus (EXO), etc.
- hb5 table: Contains Chinese Bible verses
- kjv table: Contains English Bible verses (King James Version)

Make sure the database file is named exactly "bible.db"