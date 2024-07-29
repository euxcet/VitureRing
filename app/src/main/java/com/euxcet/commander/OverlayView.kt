package com.euxcet.commander

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.view.View

class OverlayView(context: Context) : View(context) {
    companion object {
        const val EYE_CIRCLE_RADIUS = 40.0f
        const val EYE_CIRCLE_STROKE_WIDTH = 8f
    }

    private val paint = Paint()
    private val leftEyePaint = Paint()
    private val rightEyePaint = Paint()
    private val path = Path()
    private var s = 50f
    private val leftEyeX = 400f
    private val leftEyeY = 400f

    private val rightEyeX = 200f
    private val rightEyeY = 100f

    init {
        z = 100.0f
        paint.isAntiAlias = true
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20f
        paint.textSize = 52f

        leftEyePaint.isAntiAlias = true
        leftEyePaint.color = Color.RED
        leftEyePaint.style = Paint.Style.STROKE
        leftEyePaint.strokeWidth = EYE_CIRCLE_STROKE_WIDTH

        rightEyePaint.isAntiAlias = true
        rightEyePaint.color = Color.GREEN
        rightEyePaint.style = Paint.Style.STROKE
        rightEyePaint.strokeWidth = EYE_CIRCLE_STROKE_WIDTH

        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas.drawCircle(leftEyeX, leftEyeY, EYE_CIRCLE_RADIUS, leftEyePaint)
        canvas.drawCircle(rightEyeX, rightEyeY, EYE_CIRCLE_RADIUS, rightEyePaint)
    }

    fun resetBoxes() { // redraw boxes
        postInvalidate()
    }

    fun resetEye() { // redraw boxes
        postInvalidate()
    }
}