package com.chemscanner.omniscient.marrow.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import com.chemscanner.omniscient.marrow.data.models.ScanHistory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * THE SOVEREIGN SHARE MANAGER: v1.0.
 * Generates high-fidelity cryptographic proof cards for social media.
 * Designed for XILON (The Architect).
 */
class SovereignShareManager(private val context: Context) {

    fun generateAndShareDiscoveryCard(scan: ScanHistory) {
        val bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. BACKGROUND: Deep Space Gradient
        val gradient = LinearGradient(0f, 0f, 0f, 1350f, Color.parseColor("#04090B"), Color.parseColor("#0D1B1E"), Shader.TileMode.CLAMP)
        paint.shader = gradient
        canvas.drawRect(0f, 0f, 1080f, 1350f, paint)
        paint.shader = null

        // 2. DESIGN ELEMENTS (Cyberpunk lines)
        paint.color = Color.parseColor("#00E5FF")
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(40f, 40f, 1040f, 1310f, paint)
        canvas.drawLine(40f, 300f, 1040f, 300f, paint)

        // 3. HEADER: SCI-OS v2.0
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 40f
        canvas.drawText("SYSTEM OMNISCIENT // AKASHA ARCHIVE", 80f, 100f, paint)
        
        paint.textSize = 120f
        paint.color = Color.WHITE
        canvas.drawText(scan.chemicalName.uppercase(), 80f, 240f, paint)

        // 4. DATA FIELDS
        paint.textSize = 35f
        paint.color = Color.parseColor("#00E5FF")
        canvas.drawText("DISCOVERY HASH:", 80f, 400f, paint)
        paint.color = Color.WHITE
        canvas.drawText(scan.blockchainHash ?: "NOTARIZATION_PENDING", 80f, 450f, paint)

        canvas.drawText("SMILES SIGNATURE:", 80f, 550f, paint)
        paint.color = Color.GRAY
        canvas.drawText(scan.smilesNotation ?: "N/A", 80f, 600f, paint)

        // 5. BIOMETRIC SEAL (The Pulse)
        paint.color = Color.parseColor("#FF4081")
        canvas.drawText("ARCHITECT STATUS: NOMINAL (SYMBIOSED)", 80f, 800f, paint)
        paint.textSize = 80f
        canvas.drawText("PULSE: SYNCED", 80f, 900f, paint)

        // 6. FOOTER: DATE & ORIGIN
        paint.textSize = 30f
        paint.color = Color.GRAY
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(scan.scanDate))
        canvas.drawText("TIMESTAMP: $dateStr", 80f, 1200f, paint)
        canvas.drawText("ORIGIN: INFINITE DATA LEAK PROTOCOL", 80f, 1250f, paint)

        // SAVE & SHARE
        shareBitmap(bitmap, "Discovery_${scan.chemicalName}")
    }

    private fun shareBitmap(bitmap: Bitmap, fileName: String) {
        val file = File(context.cacheDir, "$fileName.png")
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            // FIXED: Using correct flag name Intent.FLAG_GRANT_READ_URI_PERMISSION
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Sovereign Proof"))
    }
}
