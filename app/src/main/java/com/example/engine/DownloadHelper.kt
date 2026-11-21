package com.example.engine

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.webkit.URLUtil

/**
 * Thin wrapper around the system [DownloadManager] used by the WebView
 * download listener and the Downloads screen.
 */
object DownloadHelper {

    data class DownloadRequestInfo(
        val downloadId: Long,
        val fileName: String,
        val url: String,
        val mimeType: String,
        val sizeBytes: Long
    )

    /**
     * Enqueues a download and returns the info needed to track it locally.
     */
    fun enqueue(
        context: Context,
        downloadUrl: String,
        userAgent: String,
        contentDisposition: String?,
        mimeType: String?
    ): DownloadRequestInfo {
        val fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimeType)
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setMimeType(mimeType ?: "application/octet-stream")
            if (userAgent.isNotBlank()) addRequestHeader("User-Agent", userAgent)
            setTitle(fileName)
            setDescription("Downloaded with Nova Browser")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val id = manager.enqueue(request)
        return DownloadRequestInfo(
            downloadId = id,
            fileName = fileName,
            url = downloadUrl,
            mimeType = mimeType ?: "application/octet-stream",
            sizeBytes = -1L
        )
    }

    fun statusOf(context: Context, downloadId: Long): Int {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = manager.queryDownloads(java.util.Collections.singletonMap(DownloadManager.COLUMN_ID, downloadId))
        return try {
            cursor?.use {
                if (it.moveToFirst() && it.columnIndexOrThrow(DownloadManager.COLUMN_STATUS) >= 0) {
                    it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                } else -1
            } ?: -1
        } catch (e: Exception) {
            -1
        }
    }

    fun statusLabel(status: Int): String = when (status) {
        DownloadManager.STATUS_PAUSED -> "Paused"
        DownloadManager.STATUS_PENDING -> "Queued"
        DownloadManager.STATUS_RUNNING -> "Downloading"
        DownloadManager.STATUS_SUCCESSFUL -> "Completed"
        DownloadManager.STATUS_FAILED -> "Failed"
        else -> "Unknown"
    }
}
