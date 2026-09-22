package com.chemscanner.omniscient.ui.scanner

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.*

class ScannerHUDView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val scanPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var scanLinePos = 0.1f
    private var scanDirection = 1
    private val random = Random()

    init {
        scanPaint.strokeWidth = 4f
        scanPaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2
        val cy = h / 2

        // 1. Central Target Lock
        paint.color = Color.CYAN
        paint.alpha = 40
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawCircle(cx, cy, 150f, paint)

        paint.alpha = 150
        val bracketSize = 40f
        val offset = 80f
        // Top Left Bracket
        canvas.drawLine(cx - offset, cy - offset, cx - offset + bracketSize, cy - offset, paint)
        canvas.drawLine(cx - offset, cy - offset, cx - offset, cy - offset + bracketSize, paint)
        // Bottom Right Bracket
        canvas.drawLine(cx + offset, cy + offset, cx + offset - bracketSize, cy + offset, paint)
        canvas.drawLine(cx + offset, cy + offset, cx + offset, cy + offset - bracketSize, paint)

        // 2. Corner HUD Elements
        val margin = 60f
        val cornerLen = 80f
        paint.strokeWidth = 5f
        // Top Left
        canvas.drawLine(margin, margin, margin + cornerLen, margin, paint)
        canvas.drawLine(margin, margin, margin, margin + cornerLen, paint)
        // Top Right
        canvas.drawLine(w - margin, margin, w - margin - cornerLen, margin, paint)
        canvas.drawLine(w - margin, margin, w - margin, margin + cornerLen, paint)

        // 3. Scanning Laser
        scanPaint.shader = LinearGradient(0f, 0f, w, 0f, 
            intArrayOf(Color.TRANSPARENT, Color.CYAN, Color.TRANSPARENT), 
            null, Shader.TileMode.CLAMP)
        canvas.drawLine(0f, h * scanLinePos, w, h * scanLinePos, scanPaint)

        // 4. Random Bitstream Data
        paint.style = Paint.Style.FILL
        paint.textSize = 24f
        paint.alpha = 180
        for (i in 0..4) {
            val bitStr = if (random.nextBoolean()) "1" else "0"
            canvas.drawText("DATA_STREAM: ${bitStr.repeat(8)}", margin, h - margin - (i * 30), paint)
        }

        // Update scan line
        scanLinePos += 0.005f * scanDirection
        if (scanLinePos > 0.9f || scanLinePos < 0.1f) scanDirection *= -1
        
        invalidate()
    }
}
