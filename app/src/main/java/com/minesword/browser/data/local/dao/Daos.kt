package com.minesword.browser.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.minesword.browser.data.local.entity.BookmarkEntity
import com.minesword.browser.data.local.entity.DownloadEntity
import com.minesword.browser.data.local.entity.HistoryEntity
import com.minesword.browser.data.local.entity.SitePermissionEntity
import com.minesword.browser.data.local.entity.TabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM browsing_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM browsing_history WHERE url LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryEntity)

    @Query("DELETE FROM browsing_history WHERE id = :id")
    suspend fun deleteHistoryItem(id: Long)

    @Query("DELETE FROM browsing_history")
    suspend fun clearAllHistory()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY dateAdded DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun getBookmarkByUrl(url: String): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)
}

@Dao
interface TabDao {
    @Query("SELECT * FROM open_tabs WHERE isIncognito = 0 ORDER BY position ASC")
    fun getSavedTabs(): Flow<List<TabEntity>>

    @Query("SELECT * FROM open_tabs WHERE isIncognito = 0 ORDER BY position ASC")
    suspend fun getSavedTabsList(): List<TabEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTab(tab: TabEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTabs(tabs: List<TabEntity>)

    @Query("DELETE FROM open_tabs WHERE id = :id")
    suspend fun deleteTab(id: String)

    @Query("DELETE FROM open_tabs WHERE isIncognito = 0")
    suspend fun clearSavedTabs()
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: Long)
}

@Dao
interface SitePermissionDao {
    @Query("SELECT * FROM site_permissions WHERE origin = :origin LIMIT 1")
    suspend fun getPermission(origin: String): SitePermissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPermission(permission: SitePermissionEntity)
}
