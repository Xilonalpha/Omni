package com.chemscanner.omniscient.marrow.services

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.ModelDownloadStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE SOVEREIGN MODEL DOWNLOADER.
 * Part of the SOVEREIGN MARROW SDK (.aar).
 * Responsible for localizing Ana Istla's 1.5GB Neural Core.
 */
@Singleton
class ModelDownloaderService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val globalKnowledge: GlobalKnowledgeRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val MODEL_URL = "https://example.com/gemma-2b-it-cpu-int4.bin" // Placeholder for actual source
    private val FILE_NAME = "gemma-2b-it-cpu-int4.bin"

    fun checkAndDownloadModels() {
        val modelFile = File(context.filesDir, FILE_NAME)
        if (modelFile.exists() && modelFile.length() > 1000000) {
            globalKnowledge.updateDownload(ModelDownloadStatus(FILE_NAME, 100f, false))
            return
        }

        startDownload()
    }

    private fun startDownload() {
        val request = DownloadManager.Request(Uri.parse(MODEL_URL))
            .setTitle("Asimilare Creier Ana Istla")
            .setDescription("Sincronizare Măduvă Sovereign Core...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, null, FILE_NAME)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        monitorProgress(downloadId)
    }

    private fun monitorProgress(id: Long) {
        scope.launch {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            var downloading = true
            while (downloading && isActive) {
                val query = DownloadManager.Query().setFilterById(id)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false
                        finalizeDownload()
                    }

                    if (bytesTotal > 0) {
                        val progress = (bytesDownloaded * 100f) / bytesTotal
                        globalKnowledge.updateDownload(ModelDownloadStatus(FILE_NAME, progress, true))
                    }
                }
                cursor.close()
                delay(1000)
            }
        }
    }

    private fun finalizeDownload() {
        // Move file from external to internal storage for Marrow sovereignty
        val externalFile = File(context.getExternalFilesDir(null), FILE_NAME)
        val internalFile = File(context.filesDir, FILE_NAME)
        if (externalFile.exists()) {
            externalFile.copyTo(internalFile, overwrite = true)
            externalFile.delete()
            globalKnowledge.updateDownload(ModelDownloadStatus(FILE_NAME, 100f, false))
            globalKnowledge.logEvent("MARROW_SYNC", "Neural Core asimilated successfully.", 5)
        }
    }
}
