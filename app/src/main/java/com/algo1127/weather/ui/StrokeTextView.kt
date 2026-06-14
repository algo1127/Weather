package com.algo1127.weather.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * A TextView that supports a solid stroke (outline) around the text.
 * Default is a black fill with a white outline.
 */
class StrokeTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var strokeWidthValue = 4f
    private var strokeColorValue = Color.WHITE

    override fun onDraw(canvas: Canvas) {
        val currentTextColor = textColors

        // 1. Draw the stroke
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidthValue
        setTextColor(strokeColorValue)
        super.onDraw(canvas)

        // 2. Draw the main text (fill)
        paint.style = Paint.Style.FILL
        setTextColor(currentTextColor)
        super.onDraw(canvas)
    }
    
    fun setStroke(width: Float, color: Int) {
        strokeWidthValue = width
        strokeColorValue = color
        invalidate()
    }
}