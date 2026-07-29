package com.example.data

import com.example.engine.PluginArchitectureManager
import kotlinx.coroutines.flow.Flow

class GhostRepository(private val database: GhostDatabase) {
    val allHistory: Flow<List<BrowsingHistoryEntity>> = database.historyDao().getAllHistory()
    val allBookmarks: Flow<List<BookmarkEntity>> = database.bookmarkDao().getAllBookmarks()
    val allTabs: Flow<List<TabEntity>> = database.tabDao().getAllTabs()
    val allTabGroups: Flow<List<TabGroupEntity>> = database.tabGroupDao().getAllTabGroups()
    val allPasswords: Flow<List<PasswordVaultEntity>> = database.passwordVaultDao().getAllPasswords()
    val allPlugins: Flow<List<ExtensionPluginEntity>> = database.extensionPluginDao().getAllPlugins()

    suspend fun addHistory(title: String, url: String, faviconUrl: String = "") {
        if (url.startsWith("ghost://") || url.isBlank()) return
        val domain = try {
            android.net.Uri.parse(url).host ?: url
        } catch (e: Exception) {
            url
        }
        database.historyDao().insertHistory(
            BrowsingHistoryEntity(
                title = if (title.isBlank()) domain else title,
                url = url,
                faviconUrl = faviconUrl,
                domain = domain
            )
        )
    }

    suspend fun clearHistory() = database.historyDao().clearAllHistory()
    suspend fun deleteHistoryById(id: Long) = database.historyDao().deleteHistoryById(id)

    suspend fun addBookmark(title: String, url: String, folder: String = "Bookmarks") {
        database.bookmarkDao().insertBookmark(
            BookmarkEntity(title = title, url = url, folder = folder)
        )
    }

    suspend fun deleteBookmark(id: Long) = database.bookmarkDao().deleteBookmarkById(id)
    suspend fun deleteBookmarkByUrl(url: String) = database.bookmarkDao().deleteBookmarkByUrl(url)

    suspend fun saveTabs(tabs: List<TabEntity>) {
        database.tabDao().insertTabs(tabs)
    }

    suspend fun addTab(tab: TabEntity) = database.tabDao().insertTab(tab)
    suspend fun deleteTab(id: String) = database.tabDao().deleteTabById(id)
    suspend fun clearAllTabs() = database.tabDao().clearAllTabs()

    suspend fun saveTabGroup(group: TabGroupEntity) = database.tabGroupDao().insertTabGroup(group)
    suspend fun deleteTabGroup(id: String) = database.tabGroupDao().deleteTabGroupById(id)

    suspend fun savePassword(domain: String, username: String, plainPassword: String) {
        val encrypted = CryptoUtils.encrypt(plainPassword)
        database.passwordVaultDao().insertPassword(
            PasswordVaultEntity(
                domain = domain,
                username = username,
                encryptedPasswordIv = encrypted.ivBase64,
                encryptedPasswordData = encrypted.dataBase64
            )
        )
    }

    suspend fun deletePassword(id: Long) = database.passwordVaultDao().deletePasswordById(id)
    suspend fun clearAllPasswords() = database.passwordVaultDao().clearAllPasswords()

    suspend fun initializeDefaultPluginsIfNeeded() {
        val defaultPlugins = PluginArchitectureManager.defaultChromiumStorePlugins
        database.extensionPluginDao().insertPlugins(defaultPlugins)
    }

    suspend fun saveCustomPlugin(plugin: ExtensionPluginEntity) {
        database.extensionPluginDao().insertPlugin(plugin)
    }

    suspend fun setPluginEnabled(id: String, isEnabled: Boolean) {
        database.extensionPluginDao().setPluginEnabled(id, isEnabled)
    }

    suspend fun nukeAllUserData() {
        database.historyDao().clearAllHistory()
        database.bookmarkDao().clearAllBookmarks()
        database.tabDao().clearAllTabs()
        database.passwordVaultDao().clearAllPasswords()
    }
}
