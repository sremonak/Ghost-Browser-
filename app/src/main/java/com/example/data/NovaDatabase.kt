package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        HistoryEntity::class,
        BookmarkEntity::class,
        TabEntity::class,
        TabGroupEntity::class,
        DownloadEntity::class,
        ReadingListEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun tabDao(): TabDao
    abstract fun tabGroupDao(): TabGroupDao
    abstract fun downloadDao(): DownloadDao
    abstract fun readingListDao(): ReadingListDao

    companion object {
        @Volatile
        private var instance: NovaDatabase? = null

        fun getInstance(context: Context): NovaDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "nova-browser.db"
                ).build().also { instance = it }
            }
        }
    }
}
