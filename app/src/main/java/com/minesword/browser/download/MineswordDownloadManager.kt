package com.minesword.browser.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import com.minesword.browser.data.local.dao.DownloadDao
import com.minesword.browser.data.local.entity.DownloadEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MineswordDownloadManager(
    private val context: Context,
    private val downloadDao: DownloadDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startDownload(
        url: String,
        userAgent: String? = null,
        contentDisposition: String? = null,
        mimeType: String? = null
    ) {
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setMimeType(mimeType ?: "application/octet-stream")
            setTitle(fileName)
            setDescription("Downloading file via Minesword Browser...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            if (userAgent != null) {
                addRequestHeader("User-Agent", userAgent)
            }
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = dm.enqueue(request)

        scope.launch {
            val downloadEntity = DownloadEntity(
                id = downloadId,
                url = url,
                fileName = fileName,
                filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName",
                mimeType = mimeType ?: "unknown",
                totalBytes = 0,
                downloadedBytes = 0,
                status = "DOWNLOADING",
                timestamp = System.currentTimeMillis()
            )
            downloadDao.insertDownload(downloadEntity)
        }
    }
}
