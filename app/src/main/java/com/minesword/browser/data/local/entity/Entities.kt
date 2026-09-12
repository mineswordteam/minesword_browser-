package com.minesword.browser.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "browsing_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val visitCount: Int = 1,
    val faviconUrl: String? = null
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val folder: String = "Default",
    val dateAdded: Long = System.currentTimeMillis(),
    val faviconUrl: String? = null
)

@Entity(tableName = "open_tabs")
data class TabEntity(
    @PrimaryKey val id: String, // UUID string
    val url: String,
    val title: String,
    val isIncognito: Boolean = false,
    val lastAccessed: Long = System.currentTimeMillis(),
    val isSelected: Boolean = false,
    val position: Int = 0
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val status: String, // PENDING, DOWNLOADING, COMPLETED, FAILED, PAUSED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "site_permissions")
data class SitePermissionEntity(
    @PrimaryKey val origin: String, // e.g. "https://example.com"
    val allowJavaScript: Boolean = true,
    val allowCookies: Boolean = true,
    val allowGeolocation: Boolean = false,
    val allowCamera: Boolean = false,
    val allowMicrophone: Boolean = false
)
