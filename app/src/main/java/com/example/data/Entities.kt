package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "browsing_history")
data class BrowsingHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis(),
    val faviconUrl: String = "",
    val domain: String = ""
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val folder: String = "Bookmarks",
    val timestamp: Long = System.currentTimeMillis(),
    val faviconUrl: String = ""
)

@Entity(tableName = "tabs")
data class TabEntity(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val isIncognito: Boolean = false,
    val tabGroupId: String? = null,
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val position: Int = 0,
    val isSleeping: Boolean = false
)

@Entity(tableName = "tab_groups")
data class TabGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val isCollapsed: Boolean = false
)

@Entity(tableName = "password_vault")
data class PasswordVaultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val username: String,
    val encryptedPasswordIv: String,
    val encryptedPasswordData: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "extension_plugins")
data class ExtensionPluginEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val version: String,
    val isEnabled: Boolean = true,
    val iconName: String = "extension",
    val author: String = "Ghost Open Source Community",
    val category: String = "Privacy"
)
