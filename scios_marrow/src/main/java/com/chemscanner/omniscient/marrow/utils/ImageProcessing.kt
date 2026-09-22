package com.chemscanner.omniscient.marrow.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import android.util.Base64
import androidx.camera.core.ImageProxy
import androidx.core.graphics.scale
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * UTILITY FOR HIGH-FIDELITY IMAGE MANIPULATION.
 * AUTHORITY: ARCHITECT XILON.
 * v1.2: Fixed crash for non-YUV ImageProxy formats.
 */
object ImageProcessing {

    /**
     * Prepares an image for AI analysis by resizing and normalizing.
     */
    fun preprocessImage(imageFile: File): Bitmap {
        val originalBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        return resizeBitmap(originalBitmap, 1024) 
    }

    /**
     * Resizes a bitmap maintaining aspect ratio using KTX extension.
     */
    fun resizeBitmap(source: Bitmap, maxLength: Int): Bitmap {
        try {
            if (source.width <= maxLength && source.height <= maxLength) {
                return source
            }
            val ratio = source.width.toFloat() / source.height.toFloat()
            val newWidth: Int
            val newHeight: Int
            if (ratio > 1) {
                newWidth = maxLength
                newHeight = (maxLength / ratio).toInt()
            } else {
                newWidth = (maxLength * ratio).toInt()
                newHeight = maxLength
            }
            // Use KTX scale extension for improved performance
            return source.scale(newWidth, newHeight, true)
        } catch (e: Exception) {
            Timber.v("Resize failed: ${e.message}")
            return source
        }
    }

    /**
     * Converts a Bitmap to Base64 String for API transmission.
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Creates a cache file from an Android Uri.
     */
    fun createFileFromUri(context: Context, uri: Uri, fileName: String = "temp_image"): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            Timber.e(e, "Error creating file from URI: $uri. E: ${e.message}")
            null
        }
    }

    /**
     * Converts CameraX ImageProxy to a rotated Bitmap.
     */
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val bitmap = if (imageProxy.format == ImageFormat.JPEG) {
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            // Assume YUV_420_888 if not JPEG
            val yBuffer = imageProxy.planes[0].buffer
            val uBuffer = imageProxy.planes[1].buffer
            val vBuffer = imageProxy.planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + uSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 90, out)
            val imageBytes = out.toByteArray()
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }

        val matrix = Matrix()
        matrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
