package com.euxcet.viturering.pages.writing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.euxcet.viturering.R
import kotlinx.coroutines.Job
import kotlin.math.max
import kotlin.math.min

enum class HandwritingMode {
    CURSOR, WRITING
}

class HandwritingControlView(context: Context, attrs: AttributeSet?, defStyleAttr: Int): View(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var cursorRadius = dip2px(context, 10f).toFloat()
    private var canvasWidth = 1920f
    private var canvasHeight = 1080f
    private var cursorX = 960f
    private var cursorY = 540f
    private var mode: HandwritingMode = HandwritingMode.CURSOR
    private val pathList = mutableListOf<Path>()
    private var curPath: Path? = null
    private var onWriteSubmit: ((Bitmap) -> Unit)? = null
    private var pencilBitmap: Bitmap? = null

    init {
        viewTreeObserver.addOnGlobalLayoutListener {
            if (canvasWidth.toInt() != width || canvasHeight.toInt() != height) {
                canvasWidth = width.toFloat()
                canvasHeight = height.toFloat()
                cursorX = width / 2f
                cursorY = height / 2f
            }
        }
        val rawPencilBitmap = BitmapFactory.decodeResource(resources, R.drawable.pencil)
        pencilBitmap = Bitmap.createScaledBitmap(rawPencilBitmap, dip2px(context, 24f), dip2px(context, 24f), false)
    }

    private val cursorPaint = Paint().apply {
        isAntiAlias = true
        color = Color(0x55A2A2A2).toArgb()
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 1f
    }

    private val pathPaint: Paint = Paint().apply {
        isAntiAlias = true
        color = Color(0xFF000000).toArgb()
        style = Paint.Style.STROKE
        strokeWidth = dip2px(context, 10f).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mode == HandwritingMode.WRITING) {
            pencilBitmap?.let {
                canvas.drawBitmap(it, cursorX, cursorY - it.height, null)
            }
        } else {
            canvas.drawCircle(cursorX, cursorY, cursorRadius, cursorPaint)
        }
        pathList.forEach {
            canvas.drawPath(it, pathPaint)
        }
        curPath?.let {
            canvas.drawPath(it, pathPaint)
        }
    }

    fun reset() {
        writeSubmitJob?.cancel()
        cursorX = canvasWidth / 2f
        cursorY = canvasHeight / 2f
        mode = HandwritingMode.CURSOR
        curPath = null
        pathList.clear()
        postInvalidate()
    }


    /**
     * 设置提交结果的回调函数
     */
    fun setOnWriteSubmit(onWriteSubmit: ((Bitmap) -> Unit)?) {
        this.onWriteSubmit = onWriteSubmit
    }

    /**
     * 提交写字结果
     */
    fun submitWriting() : Bitmap? {
        var bitmap: Bitmap? = null
        if (pathList.size > 0) {
            try {
                bitmap = getWriteBitmap()?.let {
                    onWriteSubmit?.invoke(it)
                    it
                }
            } catch (e: Exception) {
                Log.e("HandwritingControlView", "submit error", e)
            }
            pathList.clear()
        }
        return bitmap
    }

    /**
     * 开始写字
     */
    fun beginWrite() {
        mode = HandwritingMode.WRITING
        writeSubmitJob?.cancel()
        curPath = Path().apply {
            moveTo(cursorX, cursorY)
        }
    }

    private var writeSubmitJob: Job? = null

    /**
     * 结束写字，结束写字1s后会将结果转化为图片通知调用方
     */
    fun endWrite() {
        mode = HandwritingMode.CURSOR
        curPath?.let { path ->
            val boundRect = RectF()
            path.computeBounds(boundRect, true)
            if (boundRect.width() > 3 || boundRect.height() > 3) {
                pathList.add(path)
            }
        }
        curPath = null
    }

    private fun getWriteBitmap(): Bitmap? {
        if (pathList.isEmpty()) {
            return null
        }
        val bitmap = Bitmap.createBitmap(canvasWidth.toInt(), canvasHeight.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color(0xFFFFFFFF).toArgb())
        val paint = pathPaint
        pathList.forEach {
            canvas.drawPath(it, paint)
        }
        return bitmap
    }

    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    private var lastSampleX = 0f
    private var lastSampleY = 0f
    fun move(x: Float, y: Float) {
        cursorX += x
        cursorY += y
        cursorX = max(min(cursorX, canvasWidth), 0f)
        cursorY = max(min(cursorY, canvasHeight), 0f)
        if (mode == HandwritingMode.WRITING) {
            if (Math.abs(cursorX - lastSampleX) > 1 || Math.abs(cursorY - lastSampleY) > 1) {
                lastSampleX = cursorX
                lastSampleY = cursorY
                curPath?.lineTo(cursorX, cursorY)
                //handwritingApi.addPoint(cursorX.toInt(), cursorY.toInt())
            }
        }
        postInvalidate()
    }

    private var isTouchDown = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // 模拟戒指滑动
        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                isTouchDown = true
                lastTouchX = event.x
                lastTouchY = event.y
                cursorX = event.x
                cursorY = event.y
                beginWrite()
                true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isTouchDown) {
                    move(event.x - lastTouchX, event.y - lastTouchY)
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
                true
            }

            MotionEvent.ACTION_UP -> {
                isTouchDown = false
                endWrite()
                true
            }

            else -> {
                super.onTouchEvent(event)
            }
        }
    }
}