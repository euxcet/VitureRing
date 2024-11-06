package com.euxcet.viturering.pages.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min

class MainControlView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : View(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var cursorRadius = dip2px(context, 20f).toFloat()
    private var canvasWidth = 1920f
    private var canvasHeight = 1080f
    private var cursorX = 960f
    private var cursorY = 540f

    init {
        viewTreeObserver.addOnGlobalLayoutListener {
            if (canvasWidth.toInt() != width || canvasHeight.toInt() != height) {
                canvasWidth = width.toFloat()
                canvasHeight = height.toFloat()
                cursorX = width / 2f
                cursorY = height / 2f
            }
        }
    }

    private val cursorPaint = Paint().apply {
        isAntiAlias = true
        color = Color(0xC4D7D7D7).toArgb()
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 1f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(cursorX, cursorY, cursorRadius, cursorPaint)
    }

    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun getCursorPoint(): PointF {
        return PointF(cursorX, cursorY)
    }

    fun move(x: Float, y: Float) {
        cursorX += x
        cursorY += y
        cursorX = max(min(cursorX, canvasWidth), 0f)
        cursorY = max(min(cursorY, canvasHeight), 0f)
        postInvalidate()
    }
}