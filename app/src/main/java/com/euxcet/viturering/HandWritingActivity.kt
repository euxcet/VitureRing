package com.euxcet.viturering

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.euxcet.viturering.input.HandwritingApi
import com.euxcet.viturering.input.XFHandwriting
import com.euxcet.viturering.utils.LanguageUtils
import com.hcifuture.producer.detector.TouchState
import com.hcifuture.producer.sensor.data.RingTouchEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class HandWritingActivity : AppCompatActivity() {

    private val TAG = "HandWritingActivity"

    @Inject
    lateinit var ringManager: RingManager

    private var controlView: HandwritingControlView? = null

    private val handwritingApi: HandwritingApi by lazy {
        XFHandwriting(this.applicationContext)
    }

    private var inputView: EditText? = null
    private val density by lazy {
        resources.displayMetrics.density
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        setContentView(R.layout.activity_hand_writing)
        controlView = findViewById<HandwritingControlView>(R.id.control_view).apply {
            setOnWriteSubmit { bitmap ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        handwritingApi.recognizeHandwriting(bitmap)?.let { word ->
                            Log.e(TAG, "Recognized word: $word")
                            withContext(Dispatchers.Main) {
                                inputView?.append(word)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Recognize handwriting failed", e)
                    }
                }
            }
        }
        connectRing()
        inputView = findViewById(R.id.input)
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            inputView?.requestFocus()
        }
    }

    private var isRingTouchDown = false
    private var endHoldJob: Job? = null

    private fun connectRing() {
        ringManager.registerListener {
            onConnectCallback { // Connect
                runOnUiThread {

                }
            }
            onGestureCallback { // Gesture
                runOnUiThread {
                    Log.e("Nuix", "Gesture: $it")
                    val gestureText = "手势: ${LanguageUtils.gestureChinese(it)}"
                    when (it) {
                        "pinch" -> {

                        }
                        "middle_pinch" -> {
                            //overlayView?.switch()
                        }
                        "snap" -> {
//                            val intent = Intent(Settings.ACTION_SETTINGS)
//                            startActivity(intent)
                        }
                        "circle_clockwise" -> {
//                            val intent = Intent(this@HomeActivity, ObjectActivity::class.java)
//                            startActivity(intent)
                            controlView?.submitWriting()
                        }
                        "circle_counterclockwise" -> {
                            finish()
                        }
                        "touch_ring" -> {
                            //overlayView?.reset()
                        }
                    }
                }
            }
            onMoveCallback { // Move
                runOnUiThread {
                    if (!isRingTouchDown) {
                        controlView?.move(it.first, it.second)
                    }
                }
            }
            onStateCallback { // State
                runOnUiThread {
//                    val statusText = "连接状态: ${LanguageUtils.statusChinese(it)}"
                    // statusView.text = statusText
                    findViewById<TextView>(R.id.ring_state).text = LanguageUtils.statusChinese(it)
                }
            }
            onTouchCallback { // Touch
//                runOnUiThread {
//                    val touchText = "触摸: ${(it.data)}"
//                    when (it.data) {
//                        RingTouchEvent.HOLD -> {
//                            if (!isRingTouchHold) {
//                                isRingTouchHold = true
//                                controlView?.beginWrite()
//                            }
//                            endHoldJob?.cancel()
//                            endHoldJob = CoroutineScope(Dispatchers.Main).launch {
//                                delay(300)
//                                controlView?.endWrite()
//                                isRingTouchHold = false
//                            }
//                        }
//                        RingTouchEvent.TAP -> {
//
//                        }
//                        else -> {}
//                    }
//                }
            }
            onPlaneEventCallback {
                runOnUiThread {
                    if (it == TouchState.DOWN) {
                        Log.e("Nuix", "Plane down")
                        Handler(Looper.getMainLooper()).postDelayed({
                            isRingTouchDown = true
                        }, 500)
                        controlView?.beginWrite()
                    } else {
                        isRingTouchDown = false
                        controlView?.endWrite()
                        Log.e("Nuix", "Plane up")
                    }
                }
            }
            onPlaneMoveCallback {
                Log.e("Nuix", "Plane move: $it")
                runOnUiThread {
                    if (isRingTouchDown) {
                        controlView?.move(it.first / density, it.second/ density)
                    }
                }
            }
        }
        ringManager.connect()
    }
}