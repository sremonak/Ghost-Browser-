package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single entry in the browsing history.
 */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val domain: String,
    val visitedAt: Long = System.currentTimeMillis()
)

/**
 * A user-saved bookmark, optionally grouped into a folder.
 */
@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val folder: String = "Bookmarks",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * A persisted browser tab so it can be restored on the next launch.
 * Incognito tabs are never persisted.
 */
@Entity(tableName = "tabs")
data class TabEntity(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val isIncognito: Boolean = false,
    val groupId: String? = null,
    val position: Int = 0,
    val lastActiveAt: Long = System.currentTimeMillis()
)

/**
 * A user-created tab group (work, personal, reading, ...).
 */
@Entity(tableName = "tab_groups")
data class TabGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String
)

/**
 * A file downloaded through the browser, tracked for the Downloads screen.
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val downloadId: Long,
    val fileName: String,
    val url: String,
    val mimeType: String,
    val sizeBytes: Long,
    val status: Int,
    val createdAt: Long = System.currentTimeMillis()
)
