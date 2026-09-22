package com.chemscanner.omniscient.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // CHANGED: Use cacheDir or filesDir for internal app use to avoid MediaProvider issues on Android 11+
        val storageDir = context.getExternalFilesDir(null) ?: context.filesDir
        
        return File.createTempFile(
            "CHEM_SCAN_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
    
    fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
    }
    
    fun getAppStorageDirectory(context: Context): File {
        // CHANGED: Using internal filesDir to avoid "Inserting private file" exception if this is used with MediaProvider
        return File(context.filesDir, "OmniscientScanner").apply {
            if (!exists()) mkdirs()
        }
    }
    
    fun getScansDirectory(context: Context): File {
        return File(getAppStorageDirectory(context), "Scans").apply {
            if (!exists()) mkdirs()
        }
    }
    
    fun getARDirectory(context: Context): File {
        return File(getAppStorageDirectory(context), "AR").apply {
            if (!exists()) mkdirs()
        }
    }
    
    fun cleanOldFiles(context: Context, days: Int = 30) {
        val directory = getScansDirectory(context)
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        
        directory.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) {
                file.delete()
            }
        }
    }
}