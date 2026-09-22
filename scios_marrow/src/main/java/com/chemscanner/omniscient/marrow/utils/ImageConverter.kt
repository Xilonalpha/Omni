package com.chemscanner.omniscient.marrow.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import java.io.ByteArrayOutputStream

/**
 * THE OMNISCIENT IMAGE CONVERTER v1.1.
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Handles conversion from ARCore's YUV_420_888 format to standard Bitmap.
 * v1.1: Fixed package mismatch and added buffer safety.
 */
object ImageConverter {

    /**
     * Converts an android.media.Image (YUV_420_888) to a standard Bitmap.
     */
    fun toBitmap(image: Image): Bitmap? {
        if (image.format != ImageFormat.YUV_420_888) {
            return null
        }

        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + uSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)
        val imageBytes = out.toByteArray()
        
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
