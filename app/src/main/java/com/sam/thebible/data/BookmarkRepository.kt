package com.sam.thebible.data

import com.sam.thebible.data.model.Bookmark

class BookmarkRepository {
    // ...existing code...

    suspend fun updateBookmark(bookmark: Bookmark) {
        // 更新数据库中的书签
        // ...具体实现...
    }

    suspend fun deleteBookmark(bookmark: Bookmark) {
        // 删除数据库中的书签
        // ...具体实现...
    }

    // ...existing code...
}

