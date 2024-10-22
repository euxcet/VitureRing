package com.euxcet.viturering

import android.content.pm.ActivityInfo
import android.os.Bundle
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
                        }
                        "touch_ring" -> {
                            //overlayView?.reset()
                        }
                    }
                }
            }
            onMoveCallback { // Move
                runOnUiThread {
                    controlView?.move(it.first, it.second)
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
                runOnUiThread {
                    val touchText = "触摸: ${(it.data)}"
//                    touchView.text = touchText
//                    when (it.data) {
//                        RingTouchEvent.BOTTOM_BUTTON_CLICK -> {
//                            overlayView?.reset()
//                        }
//                        RingTouchEvent.TAP -> {
//                            overlayView?.reset()
//                        }
//                        else -> {}
//                    }
                }
            }
        }
        ringManager.connect()
    }
}