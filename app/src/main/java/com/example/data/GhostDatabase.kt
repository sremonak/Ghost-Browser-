package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BrowsingHistoryEntity::class,
        BookmarkEntity::class,
        TabEntity::class,
        TabGroupEntity::class,
        PasswordVaultEntity::class,
        ExtensionPluginEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GhostDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun tabDao(): TabDao
    abstract fun tabGroupDao(): TabGroupDao
    abstract fun passwordVaultDao(): PasswordVaultDao
    abstract fun extensionPluginDao(): ExtensionPluginDao

    companion object {
        @Volatile
        private var INSTANCE: GhostDatabase? = null

        fun getInstance(context: Context): GhostDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GhostDatabase::class.java,
                    "ghost_browser.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
