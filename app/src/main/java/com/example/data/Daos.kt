package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM browsing_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<BrowsingHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: BrowsingHistoryEntity)

    @Query("DELETE FROM browsing_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM browsing_history")
    suspend fun clearAllHistory()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    @Query("DELETE FROM bookmarks")
    suspend fun clearAllBookmarks()
}

@Dao
interface TabDao {
    @Query("SELECT * FROM tabs ORDER BY position ASC")
    fun getAllTabs(): Flow<List<TabEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: TabEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTabs(tabs: List<TabEntity>)

    @Query("DELETE FROM tabs WHERE id = :id")
    suspend fun deleteTabById(id: String)

    @Query("DELETE FROM tabs WHERE isIncognito = 1")
    suspend fun clearIncognitoTabs()

    @Query("DELETE FROM tabs")
    suspend fun clearAllTabs()
}

@Dao
interface TabGroupDao {
    @Query("SELECT * FROM tab_groups")
    fun getAllTabGroups(): Flow<List<TabGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTabGroup(group: TabGroupEntity)

    @Query("DELETE FROM tab_groups WHERE id = :id")
    suspend fun deleteTabGroupById(id: String)
}

@Dao
interface PasswordVaultDao {
    @Query("SELECT * FROM password_vault ORDER BY domain ASC")
    fun getAllPasswords(): Flow<List<PasswordVaultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(entry: PasswordVaultEntity)

    @Query("DELETE FROM password_vault WHERE id = :id")
    suspend fun deletePasswordById(id: Long)

    @Query("DELETE FROM password_vault")
    suspend fun clearAllPasswords()
}

@Dao
interface ExtensionPluginDao {
    @Query("SELECT * FROM extension_plugins")
    fun getAllPlugins(): Flow<List<ExtensionPluginEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlugin(plugin: ExtensionPluginEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlugins(plugins: List<ExtensionPluginEntity>)

    @Query("UPDATE extension_plugins SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setPluginEnabled(id: String, isEnabled: Boolean)
}
