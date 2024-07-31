package com.euxcet.commander

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.util.Log
import android.view.View
import com.seveninvensun.sdk.EyeData

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
    private var leftEyeX = 100f
    private var leftEyeY = 100f

    private var rightEyeX = 100f
    private var rightEyeY = 100f

    private var frameWidth: Int = 0
    private var frameHeight: Int = 0
    private var frameMargin: Int = 0
    private var windowWidth: Int = 0
    private var windowHeight: Int = 0

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

    fun setParams(width: Int, height: Int, margin: Int, wWidth: Int, wHeight: Int) {
        frameWidth = width
        frameHeight = height
        frameMargin = margin
        windowWidth = wWidth
        windowHeight = wHeight
        leftEyeX = 0.9f * windowWidth
        leftEyeY = 0.9f * windowHeight
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas.drawPath(path, paint)
        canvas.drawCircle(leftEyeX, leftEyeY, EYE_CIRCLE_RADIUS, leftEyePaint)
        canvas.drawCircle(rightEyeX, rightEyeY, EYE_CIRCLE_RADIUS, rightEyePaint)
    }

    private fun mappingPoint(x: Float, y: Float) : Pair<Float, Float> {
        return Pair(x * frameWidth + frameMargin, y * frameHeight)
    }

    fun resetBoxes(boxes: List<ObjectBox>) { // redraw boxes
        path.reset()
        for (box in boxes) {
            val p0 = mappingPoint(box.x0, box.y0)
            val p1 = mappingPoint(box.x0, box.y1)
            val p2 = mappingPoint(box.x1, box.y1)
            val p3 = mappingPoint(box.x1, box.y0)
            path.moveTo(p0.first, p0.second)
            path.lineTo(p1.first, p1.second)
            path.lineTo(p2.first, p2.second)
            path.lineTo(p3.first, p3.second)
            path.lineTo(p0.first, p0.second)
        }
        postInvalidate()
    }

    fun resetEye(eyeData: EyeData) { // redraw boxes
        if (eyeData.leftGaze.smoothPoint.x > 0.001f) {
            leftEyeX = eyeData.leftGaze.smoothPoint.x * windowWidth
            leftEyeY = eyeData.leftGaze.smoothPoint.y * windowHeight
        }
        if (eyeData.rightGaze.smoothPoint.x > 0.001f) {
            rightEyeX = eyeData.rightGaze.smoothPoint.x * windowWidth
            rightEyeY = eyeData.rightGaze.smoothPoint.y * windowHeight
        }
        postInvalidate()
    }
}