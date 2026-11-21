package com.example.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for all locally persisted browser data.
 */
class NovaRepository(private val database: NovaDatabase) {

    val allHistory: Flow<List<HistoryEntity>> = database.historyDao().getAllHistory()
    val allBookmarks: Flow<List<BookmarkEntity>> = database.bookmarkDao().getAllBookmarks()
    val allTabs: Flow<List<TabEntity>> = database.tabDao().getAllTabs()
    val allTabGroups: Flow<List<TabGroupEntity>> = database.tabGroupDao().getAllTabGroups()
    val allDownloads: Flow<List<DownloadEntity>> = database.downloadDao().getAllDownloads()

    // ------------------------------------------------------------------ history

    suspend fun addHistory(title: String, url: String) {
        if (url.isBlank()) return
        val domain = domainOf(url)
        database.historyDao().insertHistory(
            HistoryEntity(
                title = if (title.isBlank()) domain else title,
                url = url,
                domain = domain
            )
        )
    }

    suspend fun clearHistory() = database.historyDao().clearAllHistory()
    suspend fun deleteHistoryById(id: Long) = database.historyDao().deleteHistoryById(id)

    // ----------------------------------------------------------------- bookmarks

    suspend fun addBookmark(title: String, url: String, folder: String = "Bookmarks") {
        database.bookmarkDao().insertBookmark(
            BookmarkEntity(title = if (title.isBlank()) url else title, url = url, folder = folder)
        )
    }

    suspend fun removeBookmark(id: Long) = database.bookmarkDao().deleteBookmarkById(id)
    suspend fun removeBookmarkByUrl(url: String) = database.bookmarkDao().deleteBookmarkByUrl(url)
    suspend fun clearBookmarks() = database.bookmarkDao().clearAllBookmarks()
    suspend fun bookmarkFolders(): List<String> = database.bookmarkDao().getAllFolders()

    // ---------------------------------------------------------------------- tabs

    suspend fun saveTabs(tabs: List<TabEntity>) = database.tabDao().insertTabs(tabs)
    suspend fun addTab(tab: TabEntity) = database.tabDao().insertTab(tab)
    suspend fun deleteTab(id: String) = database.tabDao().deleteTabById(id)
    suspend fun clearAllTabs() = database.tabDao().clearAllTabs()

    // ---------------------------------------------------------------- tab groups

    suspend fun saveTabGroup(group: TabGroupEntity) = database.tabGroupDao().insertTabGroup(group)
    suspend fun deleteTabGroup(id: String) = database.tabGroupDao().deleteTabGroupById(id)

    // ------------------------------------------------------------------ downloads

    suspend fun addDownload(download: DownloadEntity) = database.downloadDao().insertDownload(download)
    suspend fun removeDownload(id: Long) = database.downloadDao().deleteDownloadById(id)
    suspend fun clearDownloads() = database.downloadDao().clearAllDownloads()

    // ------------------------------------------------------------------ helpers

    /**
     * Removes every trace of user browsing data from the local database.
     */
    suspend fun clearAllUserData() {
        database.historyDao().clearAllHistory()
        database.bookmarkDao().clearAllBookmarks()
        database.downloadDao().clearAllDownloads()
        database.tabDao().clearAllTabs()
    }

    private fun domainOf(url: String): String {
        return try {
            Uri.parse(url).host ?: url
        } catch (e: Exception) {
            url
        }
    }
}
