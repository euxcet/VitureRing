package com.euxcet.viturering

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.Log
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

enum class OverlayMode {
    CURSOR, MOVE, ROTATE, SCALE
}

class OverlayObject(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    var angle: Float,
    var selected: Boolean,
    var color: Int,
    var scale: Float = 0.0f,
) {
    fun getRealScale(): Float {
        return if (scale < 0) {
            1.0f / (1.0f - scale)
        } else {
            1.0f + scale
        }
    }
}

class OverlayView(context: Context) : View(context) {
    companion object {
        const val ORIGIN_X = 800.0f
        const val ORIGIN_Y = 600.0f
    }

    private val cursorPaint = Paint()
    private val objectPaint = Paint()
    private var cursorX = ORIGIN_X
    private var cursorY = ORIGIN_Y
    private val objects: MutableList<OverlayObject> = mutableListOf()
    private var mode = OverlayMode.CURSOR
    private var lastMoveTimestamp: Long = 0
    private var count = 0

    init {
        cursorPaint.isAntiAlias = true
        cursorPaint.color = Color.GREEN
        cursorPaint.style = Paint.Style.FILL
        cursorPaint.strokeWidth = 5f

        objectPaint.isAntiAlias = true
        objectPaint.color = Color.RED
        objectPaint.style = Paint.Style.FILL
        objectPaint.strokeWidth = 5f

        setLayerType(LAYER_TYPE_HARDWARE, null)
        objects.add(
            OverlayObject(
                x = 1000f,
                y = 600f,
                width = 100f,
                height = 100f,
                angle = 30.0f,
                selected = false,
                color = Color.WHITE,
            )
        )
        objects.add(
            OverlayObject(
                x = 600f,
                y = 600f,
                width = 150f,
                height = 150f,
                angle = 0.0f,
                selected = false,
                color = Color.WHITE,
            )
        )
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                postInvalidate()
                delay(30)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        for (obj in objects) {
            objectPaint.color = obj.color
            if (obj.selected) {
                when (mode) {
                    OverlayMode.MOVE -> {
                        objectPaint.color = Color.RED
                    }
                    OverlayMode.ROTATE -> {
                        objectPaint.color = Color.GREEN
                    }
                    OverlayMode.SCALE -> {
                        objectPaint.color = Color.BLUE
                    }
                    else -> {}
                }
            }
            canvas.save()
            canvas.rotate(obj.angle, obj.x, obj.y)
            val scale = obj.getRealScale()
            canvas.drawRect(
                obj.x - obj.width / 2 * scale,
                obj.y - obj.height / 2 * scale,
                obj.x + obj.width / 2 * scale,
                obj.y + obj.height / 2 * scale,
                objectPaint,
            )
            canvas.restore()
        }
        canvas.drawCircle(cursorX, cursorY, 5.0f, cursorPaint)
    }

    fun move(x: Float, y: Float) {
        when (mode) {
            OverlayMode.CURSOR -> {
                cursorX += x
                cursorY += y
                cursorX = max(min(cursorX, 1500f), 0f)
                cursorY = max(min(cursorY, 1000f), 0f)
            }
            OverlayMode.MOVE -> {
                cursorX += x
                cursorY += y
                cursorX = max(min(cursorX, 1500f), 0f)
                cursorY = max(min(cursorY, 1000f), 0f)
                for (obj in objects) {
                    if (obj.selected) {
                        obj.x += x
                        obj.y += y
                        obj.x = max(min(obj.x, 1500f), 0f)
                        obj.y = max(min(obj.y, 1000f), 0f)
                    }
                }
            }
            OverlayMode.ROTATE -> {
                for (obj in objects) {
                    if (obj.selected) {
                        obj.angle += x * 0.2f
                    }
                }
            }
            OverlayMode.SCALE -> {
                for (obj in objects) {
                    if (obj.selected) {
                        obj.scale += x / 100
                    }
                }
            }
        }
//        postInvalidate()
    }

    fun select() {
        for (obj in objects) {
            if (obj.selected) {
                obj.selected = false
//                postInvalidate()
                return
            }
        }
        var minDistance = 1000000000.0f
        var minObject: OverlayObject? = null
        for (obj in objects) {
            val distance = (obj.x - cursorX) * (obj.x - cursorX) + (obj.y - cursorY) * (obj.y - cursorY)
            if (distance < minDistance) {
                minDistance = distance
                minObject = obj
            }
        }
        if (minObject != null) {
            minObject.selected = !minObject.selected
            mode = if (minObject.selected) {
                OverlayMode.MOVE
            } else {
                OverlayMode.CURSOR
            }
        }
//        postInvalidate()
    }

    fun switch() {
        when (mode) {
            OverlayMode.MOVE -> {
                mode = OverlayMode.ROTATE
            }
            OverlayMode.ROTATE -> {
                mode = OverlayMode.SCALE
            }
            OverlayMode.SCALE -> {
                mode = OverlayMode.MOVE
            }
            else -> {}
        }
//        postInvalidate()
    }

    fun switchToMove() {
        if (mode != OverlayMode.CURSOR) {
            mode = OverlayMode.MOVE
        }
//        postInvalidate()
    }

    fun switchToRotate() {
        if (mode != OverlayMode.CURSOR) {
            mode = OverlayMode.ROTATE
        }
//        postInvalidate()
    }

    fun switchToScale() {
        if (mode != OverlayMode.CURSOR) {
            mode = OverlayMode.SCALE
        }
//        postInvalidate()
    }

    fun reset() {
        for (obj in objects) {
            obj.selected = false
        }
        cursorX = ORIGIN_X
        cursorY = ORIGIN_Y
//        postInvalidate()
    }
}