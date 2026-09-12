package com.minesword.browser.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.minesword.browser.data.local.dao.BookmarkDao
import com.minesword.browser.data.local.dao.DownloadDao
import com.minesword.browser.data.local.dao.HistoryDao
import com.minesword.browser.data.local.dao.SitePermissionDao
import com.minesword.browser.data.local.dao.TabDao
import com.minesword.browser.data.local.entity.BookmarkEntity
import com.minesword.browser.data.local.entity.DownloadEntity
import com.minesword.browser.data.local.entity.HistoryEntity
import com.minesword.browser.data.local.entity.SitePermissionEntity
import com.minesword.browser.data.local.entity.TabEntity

@Database(
    entities = [
        HistoryEntity::class,
        BookmarkEntity::class,
        TabEntity::class,
        DownloadEntity::class,
        SitePermissionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MineswordDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun tabDao(): TabDao
    abstract fun downloadDao(): DownloadDao
    abstract fun sitePermissionDao(): SitePermissionDao

    companion object {
        @Volatile
        private var INSTANCE: MineswordDatabase? = null

        fun getInstance(context: Context): MineswordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MineswordDatabase::class.java,
                    "minesword_browser.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
